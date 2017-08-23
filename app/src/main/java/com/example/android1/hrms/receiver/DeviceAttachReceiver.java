package com.example.android1.hrms.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Android1 on 7/19/2017.
 */

public class DeviceAttachReceiver extends BroadcastReceiver {
    String TAG = "DeviceAttachReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                UsbDevice d = (UsbDevice)
                        intent.getExtras().get(UsbManager.EXTRA_DEVICE);


                Log.d(TAG, "device attach receiver");
                Toast.makeText(context, "device attached", Toast.LENGTH_LONG).show();
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Intent in = new Intent(context, FingerPrintReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, in, 0);

                alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime(),
                        1 * 1000,
                        pendingIntent);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
