package com.example.android1.hrms.receiver;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.example.android1.hrms.service.AttendanceService;

// make sure we use a WakefulBroadcastReceiver so that we acquire a partial wakelock
public class FingerPrintReceiver extends WakefulBroadcastReceiver {
    private static final String TAG = "FingerPrintReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "called FingerPrintReceiver");
        try {
            context.startService(new Intent(context, AttendanceService.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
