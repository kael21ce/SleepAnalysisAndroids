package com.kael21ce.sleepanalysisandroid;

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
import java.util.Locale;

import io.reactivex.annotations.NonNull;

public class PushWorker extends Worker {
    public PushWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParameters) {
        super(appContext, workerParameters);
    }

    private static final String TAG = PushWorker.class.getSimpleName();
    private static final String CHECK_CHANNEL_ID = "check_recommend";
    private NotificationManager mNotifyManager;
    private static final int NOTIFICATION_ID = 0;
    long now = 0, sleepStart = 0, oneHour = 1000 * 60 * 60;
    MainActivity mainActivity;
    SharedPreferences sharedPref;


    //Compare the current time and recommended sleep -> Send push notification
    @androidx.annotation.NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        now = System.currentTimeMillis();
        //sharedPref = context.getSharedPreferences("SleepWake", Context.MODE_PRIVATE);
        //sleepStart = sharedPref.getLong("mainSleepStart", 0);

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
                .setAutoCancel(true);
        createNotificationChannel(context);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        //Get the data from MainActivity
        try {
            sleepStart = getInputData().getLong("recommendOnset", 0);

            //Push notification before 1 hour from the recommended onset
            long delta = sleepStart - now;
            Log.v(TAG, "Difference from the recommended onset: " + delta);
            if (delta >= 0 && delta <= oneHour) {
                //Send push notification
                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    Log.v(TAG, "No permission");
                    return Result.failure();
                }
                Log.v(TAG, "Permission exists");
                notificationManager.notify(NOTIFICATION_ID, builder.build());

                return Result.success();
            } else {
                Log.v(TAG, "Retry");
                return Result.retry();
            }

        } catch (Exception e) {
            return Result.failure();
        }
    }

    public void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHECK_CHANNEL_ID,
                    "Check Recommendation", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
