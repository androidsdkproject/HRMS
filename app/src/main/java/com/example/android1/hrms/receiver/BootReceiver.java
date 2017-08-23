package com.example.android1.hrms.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

/**
 * Created by Android1 on 7/20/2017.
 */

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "boot receiver start");
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent in = new Intent(context, FingerPrintReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, in, 0);


            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime(),
                    1 * 1000,
                    pendingIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

