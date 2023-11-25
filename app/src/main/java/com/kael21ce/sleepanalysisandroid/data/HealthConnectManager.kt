package com.kael21ce.sleepanalysisandroid.data

import android.content.Context
import android.os.Build
import android.provider.Settings.Global
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_AVAILABLE
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.changes.Change
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ChangesTokenRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Mass
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.future.future
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZonedDateTime
import java.util.Date
import java.util.Locale
import java.util.concurrent.CompletableFuture
import kotlin.random.Random

// The minimum android level that can use Health Connect
const val MIN_SUPPORTED_SDK = Build.VERSION_CODES.O_MR1

val JAVPERMISSIONS = arrayOf(
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getWritePermission(SleepSessionRecord::class)
)


/**
 * Demonstrates reading and writing from Health Connect.
 */
class HealthConnectManager(private val context: Context) {
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    var isSleepDone = false
    var isAddSleepDone = false
    public fun getIsSleepDone(): Boolean{
        return isSleepDone
    }

    public fun getIsAddSleepDone(): Boolean{
        return isAddSleepDone
    }

    public fun setIsSleepDone(isSleepDone1: Boolean){
        this.isSleepDone = isSleepDone1
    }

    public fun setIsAddSleepDone(isAddSleepDone1: Boolean){
        this.isAddSleepDone = isAddSleepDone1
    }

    var availability = mutableStateOf(HealthConnectAvailability.NOT_SUPPORTED)
        private set

    init {
        checkAvailability()
    }

    fun checkAvailability() {
        availability.value = when {
            HealthConnectClient.getSdkStatus(context) == SDK_AVAILABLE -> HealthConnectAvailability.INSTALLED
            isSupported() -> HealthConnectAvailability.NOT_INSTALLED
            else -> HealthConnectAvailability.NOT_SUPPORTED
        }
    }

    /**
     * Determines whether all the specified permissions are already granted. It is recommended to
     * call [PermissionController.getGrantedPermissions] first in the permissions flow, as if the
     * permissions are already granted then there is no need to request permissions via
     * [PermissionController.createRequestPermissionResultContract].
     */
    suspend fun hasAllPermissions(): Boolean {
        val permissions = setOf(HealthPermission.getReadPermission(SleepSessionRecord::class),
                HealthPermission.getWritePermission(SleepSessionRecord::class))
        return healthConnectClient.permissionController.getGrantedPermissions().containsAll(permissions)
    }

    fun javHasAllPermissions(): CompletableFuture<Boolean> = GlobalScope.future { hasAllPermissions(); }

    fun requestPermissionsActivityContract(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract()
    }

    /**
     * TODO: Writes [WeightRecord] to Health Connect.
     */
    suspend fun writeSleepInput(sleepStartMilli: Long, sleepEndMilli: Long) {
        val sleepStart = Instant.ofEpochMilli(sleepStartMilli)
        val sleepEnd = Instant.ofEpochMilli(sleepEndMilli)
        val time = ZonedDateTime.now().withNano(0)
        val sleepRecord = SleepSessionRecord(
                startTime = sleepStart,
                endTime = sleepEnd,
                startZoneOffset = time.offset,
                endZoneOffset = time.offset
        )
        val records = listOf(sleepRecord)
        try {
            healthConnectClient.insertRecords(records)
            isAddSleepDone = true
            Toast.makeText(context, "Successfully insert records", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, e.message.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    fun javWriteSleepInput(sleepStart: Long, sleepEnd: Long){
        GlobalScope.future{writeSleepInput(sleepStart, sleepEnd)}
    }

    /**
     * TODO: Reads in existing [WeightRecord]s.
     */
    suspend fun readSleepInputs(start: Instant, end: Instant){
        val sdfDateTime = SimpleDateFormat("yyyy/MM/dd HH:mm")
        Log.v("start", start.toString())
        Log.v("end", end.toString())
        val request = ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end)
        )

        val response = healthConnectClient.readRecords(request)
        val db = Room.databaseBuilder(context, AppDatabase::class.java, "sleep_wake").build()
        val userDao = db.sleepDao()
        val sleepList = mutableListOf<Sleep>()
        for (sleepRecord in response.records) {
            var sleepStart = Date.from(sleepRecord.startTime).time
            val sleepEnd = Date.from(sleepRecord.endTime).time
            Log.v("THE RECORD START", sdfDateTime.format(Date.from(sleepRecord.startTime)))
            Log.v("THE RECORD END", sdfDateTime.format(Date.from(sleepRecord.endTime)))
            //check whether we need to divide the sleep to two
            val sleepStartDay = ((sleepStart + (1000*60*60*9)) / (1000 * 60 * 60 * 24))
            val sleepEndDay = ((sleepEnd + (1000*60*60*9))/ (1000 * 60 * 60 * 24))
            if (sleepStartDay != sleepEndDay) {
                val midnight = sleepEndDay * (1000 * 60 * 60 * 24)
                val additionalSleep = Sleep()
                additionalSleep.sleepStart = sleepStart
                additionalSleep.sleepEnd = midnight - (1000*60*60*9)
                sleepList.add(additionalSleep)
                sleepStart = midnight - (1000*60*60*9)
            }
            //save everything in the database
            val sleep = Sleep()
            sleep.sleepStart = sleepStart
            sleep.sleepEnd = sleepEnd
            Log.v("sleeprecord", sdfDateTime.format(Date(sleep.sleepStart)))
            Log.v("sleeprecord2", sdfDateTime.format(Date(sleep.sleepEnd)))
            sleepList.add(sleep)
        }
        userDao.insertAll(sleepList)
        isSleepDone = true
    }

    fun javReadSleepInputs(start: Instant, end: Instant){
        GlobalScope.future{readSleepInputs(start, end)}
    }

    /**
     * TODO: Returns the weekly average of [WeightRecord]s.
     */
    suspend fun computeWeeklyAverage(start: Instant, end: Instant): Mass? {
        val request = AggregateRequest(
                metrics = setOf(WeightRecord.WEIGHT_AVG),
                timeRangeFilter = TimeRangeFilter.between(start, end)
        )
        val response = healthConnectClient.aggregate(request)
        return response[WeightRecord.WEIGHT_AVG]
    }

    /**
     * TODO: Obtains a list of [ExerciseSessionRecord]s in a specified time frame. An Exercise Session Record is a
     * period of time given to an activity, that would make sense to a user, e.g. "Afternoon run"
     * etc. It does not necessarily mean, however, that the user was *running* for that entire time,
     * more that conceptually, this was the activity being undertaken.
     */
    suspend fun readExerciseSessions(start: Instant, end: Instant): List<ExerciseSessionRecord> {
        val request = ReadRecordsRequest(
                recordType = ExerciseSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end)
        )
        val response = healthConnectClient.readRecords(request)
        return response.records
    }

    /**
     * TODO: Writes an [ExerciseSessionRecord] to Health Connect.
     */
    suspend fun writeExerciseSession(start: ZonedDateTime, end: ZonedDateTime) {
        healthConnectClient.insertRecords(
                listOf(
                        ExerciseSessionRecord(
                                startTime = start.toInstant(),
                                startZoneOffset = start.offset,
                                endTime = end.toInstant(),
                                endZoneOffset = end.offset,
                                exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_RUNNING,
                                title = "My Run #${Random.nextInt(0, 60)}"
                        ),
                        StepsRecord(
                                startTime = start.toInstant(),
                                startZoneOffset = start.offset,
                                endTime = end.toInstant(),
                                endZoneOffset = end.offset,
                                count = (1000 + 1000 * Random.nextInt(3)).toLong()
                        ),
                        TotalCaloriesBurnedRecord(
                                startTime = start.toInstant(),
                                startZoneOffset = start.offset,
                                endTime = end.toInstant(),
                                endZoneOffset = end.offset,
                                energy = Energy.calories((140 + Random.nextInt(20)) * 0.01)
                        )
                ) + buildHeartRateSeries(start, end)
        )
    }

    /**
     * TODO: Build [HeartRateRecord].
     */
    private fun buildHeartRateSeries(
            sessionStartTime: ZonedDateTime,
            sessionEndTime: ZonedDateTime,
    ): HeartRateRecord {
        val samples = mutableListOf<HeartRateRecord.Sample>()
        var time = sessionStartTime
        while (time.isBefore(sessionEndTime)) {
            samples.add(
                    HeartRateRecord.Sample(
                            time = time.toInstant(),
                            beatsPerMinute = (80 + Random.nextInt(80)).toLong()
                    )
            )
            time = time.plusSeconds(30)
        }
        return HeartRateRecord(
                startTime = sessionStartTime.toInstant(),
                startZoneOffset = sessionStartTime.offset,
                endTime = sessionEndTime.toInstant(),
                endZoneOffset = sessionEndTime.offset,
                samples = samples
        )
    }


    /**
     * Obtains a changes token for the specified record types.
     */
    suspend fun getChangesToken(): String {
        return healthConnectClient.getChangesToken(
                ChangesTokenRequest(
                        setOf(
                                ExerciseSessionRecord::class,
                                StepsRecord::class,
                                TotalCaloriesBurnedRecord::class,
                                HeartRateRecord::class,
                                WeightRecord::class
                        )
                )
        )
    }

    /**
     * Retrieve changes from a changes token.
     */
    suspend fun getChanges(token: String): Flow<ChangesMessage> = flow {
        var nextChangesToken = token
        do {
            val response = healthConnectClient.getChanges(nextChangesToken)
            if (response.changesTokenExpired) {
                // As described here: https://developer.android.com/guide/health-and-fitness/health-connect/data-and-data-types/differential-changes-api
                // tokens are only valid for 30 days. It is important to check whether the token has
                // expired. As well as ensuring there is a fallback to using the token (for example
                // importing data since a certain date), more importantly, the app should ensure
                // that the changes API is used sufficiently regularly that tokens do not expire.
                throw IOException("Changes token has expired")
            }
            emit(ChangesMessage.ChangeList(response.changes))
            nextChangesToken = response.nextChangesToken
        } while (response.hasMore)
        emit(ChangesMessage.NoMoreChanges(nextChangesToken))
    }

    /**
     * Convenience function to reuse code for reading data.
     */
    private suspend inline fun <reified T : Record> readData(
            timeRangeFilter: TimeRangeFilter,
            dataOriginFilter: Set<DataOrigin> = setOf(),
    ): List<T> {
        val request = ReadRecordsRequest(
                recordType = T::class,
                dataOriginFilter = dataOriginFilter,
                timeRangeFilter = timeRangeFilter
        )
        return healthConnectClient.readRecords(request).records
    }

    private fun isSupported() = Build.VERSION.SDK_INT >= MIN_SUPPORTED_SDK

    // Represents the two types of messages that can be sent in a Changes flow.
    sealed class ChangesMessage {
        data class NoMoreChanges(val nextChangesToken: String) : ChangesMessage()
        data class ChangeList(val changes: List<Change>) : ChangesMessage()
    }
}

/**
 * Health Connect requires that the underlying Health Connect APK is installed on the device.
 * [HealthConnectAvailability] represents whether this APK is indeed installed, whether it is not
 * installed but supported on the device, or whether the device is not supported (based on Android
 * version).
 */
enum class HealthConnectAvailability {
    INSTALLED,
    NOT_INSTALLED,
    NOT_SUPPORTED
}