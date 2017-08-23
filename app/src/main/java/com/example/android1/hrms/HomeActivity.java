package com.example.android1.hrms;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.android1.hrms.database.EmployeeDatabaseHelper;
import com.example.android1.hrms.fragment.AttendanceShowListFragment;
import com.example.android1.hrms.fragment.EmployeeShowListFragment;
import com.example.android1.hrms.receiver.FingerPrintReceiver;
import com.example.android1.hrms.service.AttendanceService;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    final public static int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    EmployeeDatabaseHelper employeeDatabaseHelper;
    String TAG = "Home";
    DrawerLayout drawer;
    int intervalInMinutes = 1;
    AlarmManager alarmManager;
    Intent fingerPrintIntent;
    PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (!hasPermissions(HomeActivity.this, PERMISSIONS)) {
            Log.d(TAG, "permission");
            ActivityCompat.requestPermissions(HomeActivity.this, PERMISSIONS, PERMISSION_ALL);
        }

        employeeDatabaseHelper = new EmployeeDatabaseHelper(this);

        startServiceReceiver();


        try {
            String data = "";
            data = getIntent().getStringExtra("data");

            if (data.equals("reg")) {

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Employee List");
                }

                Fragment fragment = new EmployeeShowListFragment();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, fragment);
                ft.commit();


            } else if (data.equals("attendance")) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Attendance List");
                }
                Fragment fragment = new AttendanceShowListFragment();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, fragment);
                ft.commit();
            }

        } catch (NullPointerException e) {
            Fragment fragment = new EmployeeShowListFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }


        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


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
                    intervalInMinutes * 1000, // 60000 = 1 minute
                    pendingIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public boolean hasPermissions(Context context, String... permissions) {


        if (android.os.Build.VERSION.SDK_INT >= 21 && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        Fragment fragment = null;
        if (id == R.id.Attendance) {
            Intent in = new Intent(HomeActivity.this, DailyAttendanceActivity.class);
            startActivity(in);

        } else if (id == R.id.employeeList) {
            fragment = new EmployeeShowListFragment();
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Employee List");
            }
        } else if (id == R.id.AttendanceList) {
            fragment = new AttendanceShowListFragment();
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Attendance List");
            }

        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        alert();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);//Menu Resource, Menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.exit:
                alert();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void alert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setMessage("Do you want to exit from HRMS ?")
                .setCancelable(false)
                .setTitle("HRMS")
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        finishAffinity();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            //  to hide keyboard
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            View v = getCurrentFocus();
            if (v == null) {
                Log.d(TAG, "v ==null");

            }

            inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

        } catch (Exception e) {
            e.printStackTrace();
        }

        startService(new Intent(HomeActivity.this, AttendanceService.class));
//        startServiceReceiver();
    }
}
