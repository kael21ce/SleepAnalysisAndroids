package com.kael21ce.sleepanalysisandroid;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import io.reactivex.annotations.NonNull;

public class PushWorker extends Worker {
    public PushWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParameters) {
        super(appContext, workerParameters);
    }

    private static final String TAG = PushWorker.class.getSimpleName();
    private static final String CHECK_CHANNEL_ID = "check_recommend";
    private static final int NOTIFICATION_ID = 0;


    //Compare the current time and recommended sleep -> Send push notification
    @androidx.annotation.NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        try {
            //Code for notification
            sendNotification(context);
            return Result.success();

        } catch (Exception e) {
            return Result.failure();
        }
    }

    public void sendNotification(Context context) {
        //Set the intent called when the notification is tapped
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHECK_CHANNEL_ID)
                .setSmallIcon(R.drawable.moon)
                .setContentTitle("SleepWake")
                .setContentText("추천 수면 시간을 확인해주세요")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "No permission");
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
