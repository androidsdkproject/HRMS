package com.example.android1.hrms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android1.hrms.database.EmployeeDatabaseHelper;
import com.example.android1.hrms.modelclass.EmployeeDataModelClass;
import com.example.android1.hrms.receiver.FingerPrintReceiver;
import com.example.android1.hrms.service.AttendanceService;

public class ShowAttendanceServiceActivity extends AppCompatActivity {

    EmployeeDatabaseHelper employeeDatabaseHelper;
    EmployeeDataModelClass employeeDataModelClass;
    TextView logs;

    AlarmManager alarmManager;
    Intent fingerPrintIntent;
    PendingIntent pendingIntent;
    String TAG = "service activity";
    int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_service_activity);

        logs = (TextView) findViewById(R.id.logs);
        employeeDatabaseHelper = new EmployeeDatabaseHelper(this);
        int id = getIntent().getIntExtra("id", 1);
        int test = getIntent().getIntExtra("test", 11);

//        Toast.makeText(getApplicationContext(),test+" ",Toast.LENGTH_LONG).show();
//        logs.append(id + "\n");


        Cursor c = employeeDatabaseHelper.getEmployee(id);
        if (c != null) {
            while (c.moveToNext()) {
                employeeDataModelClass = new EmployeeDataModelClass(c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getBlob(5), c.getBlob(6));
//                logs.append(employeeDataModelClass.getEmployeeName() + "\n");
//                logs.append(employeeDataModelClass.getEmployeeEmail() + "\n");
//                logs.append(employeeDataModelClass.getEmployeeMobile() + "\n");


            }

        }

        Bitmap profileBitmap = BitmapFactory.decodeByteArray(employeeDataModelClass.getProfilebyte(), 0, employeeDataModelClass.getProfilebyte().length);

        AlertDialogeAttendance(profileBitmap);


        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {


//                Intent in = new Intent(ShowImageAndFingerPrintActivity.this, HomeActivity.class);
//                in.putExtra("data", "attendance");
//                startActivity(in);
                finish();
                startService(new Intent(ShowAttendanceServiceActivity.this, AttendanceService.class));
                startServiceReceiver();
            }
        }, SPLASH_TIME_OUT);
    }


    private void startServiceReceiver() {
        Log.d(TAG, "startAlarmManager");


        try {
            Context context = getBaseContext();
            alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            fingerPrintIntent = new Intent(context, FingerPrintReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(context, 0, fingerPrintIntent, 0);

            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime(),
                    1 * 1000, // 60000 = 1 minute
                    pendingIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


    void AlertDialogeAttendance(Bitmap bitmap) {
        AlertDialog.Builder alertadd = new AlertDialog.Builder(ShowAttendanceServiceActivity.this);
        LayoutInflater factory = LayoutInflater.from(ShowAttendanceServiceActivity.this);
        final View view = factory.inflate(R.layout.alert_dialog_layout, null);
        ImageView profileAlertPic = (ImageView) view.findViewById(R.id.dialog_imageview);
        profileAlertPic.setImageBitmap(bitmap);
        alertadd.setView(view);


        alertadd.show();

    }
}
