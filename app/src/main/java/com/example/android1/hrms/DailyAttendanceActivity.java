package com.example.android1.hrms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android1.hrms.Util.CommonMethod;
import com.example.android1.hrms.database.EmployeeDatabaseHelper;
import com.example.android1.hrms.modelclass.EmployeeRegistered;
import com.example.android1.hrms.service.AttendanceService;
import com.mantra.mfs100.FingerData;
import com.mantra.mfs100.MFS100;
import com.mantra.mfs100.MFS100Event;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class DailyAttendanceActivity extends AppCompatActivity implements OnClickListener, MFS100Event {
    //ThumbCapture
    public static String _testKey = "t7L8wTG/iv02t+pgYrMQ7tt8qvU1z42nXpJDfAfsW592N4sKUHLd8A0MEV0GRxH+f4RgefEaMZALj7mgm/Thc0jNhR2CW9BZCTgeDPjC6q0W";
    ImageView Employeeprofile, EmployeeThumb;
    Bitmap mthumb;
    byte[] Verify_Template;
    int mfsVer = 41;
    String TAG = "capture Thumb";
    int minQuality = 60;
    int timeout = 10000;
    MFS100 mfs100 = null;
    TextView logs;
    Context context;
    boolean isConnectedDevice = false;
    SharedPreferences settings;
    CommonMethod.ScannerAction scannerAction = CommonMethod.ScannerAction.Capture;
    EmployeeDatabaseHelper employeeDatabaseHelper;


    AlarmManager alarmManager;
    Intent fingerPrintIntent;
    PendingIntent pendingIntent;

    ArrayList<EmployeeRegistered> arrHavingAllRegisteredEmpDetails = new ArrayList<>();
    MediaPlayer mediaPlayer = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_attendance);
        employeeDatabaseHelper = new EmployeeDatabaseHelper(this);
        mediaPlayer = new MediaPlayer();

        stopService();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Add Attendance");
        }

        initUI();
        doMatch();
        InitScanner();


        scannerAction = CommonMethod.ScannerAction.Verify;
        StartSyncCapture();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            stopService(new Intent(DailyAttendanceActivity.this, AttendanceService.class));
            finish();// close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    public void stopService() {
        Log.d(TAG, "stop service");
        stopService(new Intent(getBaseContext(), AttendanceService.class));
    }


    private void initUI() {

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        context = DailyAttendanceActivity.this.getApplicationContext();
        mfsVer = Integer.parseInt(settings.getString("MFSVer",
                String.valueOf(mfsVer)));

        CommonMethod.DeleteDirectory();
        CommonMethod.CreateDirectory();

        mfs100 = new MFS100(this, mfsVer);
        mfs100.SetApplicationContext(this);

        try {
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        } catch (Exception e) {
            Log.e("Error", e.toString());
        }


        logs = (TextView) findViewById(R.id.logs);
        ///ImageView
        Employeeprofile = (ImageView) findViewById(R.id.profilepic);
        EmployeeThumb = (ImageView) findViewById(R.id.thumbpic);


        Employeeprofile.setOnClickListener(this);
        EmployeeThumb.setOnClickListener(this);

    }

    protected void onStop() {
        UnInitScanner();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mfs100 != null) {
            mfs100.Dispose();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        InitScanner();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.thumbpic:
                stopService();
//                Toast.makeText(getApplicationContext(), "click on thumb ", Toast.LENGTH_LONG).show();
                if (isConnectedDevice) {
                    scannerAction = CommonMethod.ScannerAction.Verify;
                    StartAsyncCapture();

                } else {
                    open();
                }


                break;
        }
    }

    private void doMatch() {

        arrHavingAllRegisteredEmpDetails = new ArrayList<>();

//        byte[] thumbbyte, profilebyte;
        Cursor c = employeeDatabaseHelper.getAllEmployee();
        if (c != null) {
            while (c.moveToNext()) {
                EmployeeRegistered employeeRegistered = new EmployeeRegistered(c.getInt(0), c.getString(1), c.getBlob(6), c.getBlob(5));
                arrHavingAllRegisteredEmpDetails.add(employeeRegistered);
                SetLogOnUIThread("employeeRegistered " + employeeRegistered.employeeID);
//            thumbbyte=c.getBlob(4);
//            profilebyte=c.getBlob(5);
            }
        }


    }

    private void InitScanner() {
        try {
            int ret = mfs100.Init();
            if (ret != 0) {
                SetLogOnUIThread(mfs100.GetErrorMsg(ret));
            } else {
                SetLogOnUIThread("Init success");
                String info = "Serial: " + mfs100.GetDeviceInfo().SerialNo()
                        + " Make: " + mfs100.GetDeviceInfo().Make()
                        + " Model: " + mfs100.GetDeviceInfo().Model()
                        + "\nCertificate: " + mfs100.GetCertification();
                SetLogOnUIThread(info);
            }
        } catch (Exception ex) {
            Toast.makeText(this, "Init failed, unhandled exception",
                    Toast.LENGTH_LONG).show();
            SetLogOnUIThread("Init failed, unhandled exception");
        }
    }

    private void SetLogOnUIThread(final String str) {

        Log.d(TAG, str);
        logs.post(new Runnable() {
            public void run() {
//                logs.append(str + "\n");
//                CommonMethod.writeLog(context, "\n"+str);
            }
        });
    }

    private void SetToastUIThread(final String str) {

        Log.d(TAG, str);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void StartAsyncCapture() {
        SetLogOnUIThread("");
//		SetLogOnUIThread("\nStartTime: " + getCurrentTime());

        try {
            int ret = mfs100.StartCapture(minQuality, timeout, true);
            if (ret != 0) {
                SetLogOnUIThread(mfs100.GetErrorMsg(ret));
            } else {
                SetLogOnUIThread("Place finger on scanner");
            }
        } catch (Exception ex) {
            SetLogOnUIThread("Error");
        }
    }

    private void StopAsynCapture() {
        mfs100.StopCapture();
    }

    private void StartSyncCapture() {

        // //// Use thread if you want to show preview, else no need to use
        // thread.
        new Thread(new Runnable() {

            @Override
            public void run() {
                SetLogOnUIThread("");
                try {
                    FingerData fingerData = new FingerData();
                    int ret = mfs100.AutoCapture(fingerData, timeout, false,
                            true);
                    if (ret != 0) {
                        SetLogOnUIThread(mfs100.GetErrorMsg(ret));
                    } else {
                        final Bitmap bitmap = BitmapFactory.decodeByteArray(
                                fingerData.FingerImage(), 0,
                                fingerData.FingerImage().length);
                        EmployeeThumb.post(new Runnable() {
                            @Override
                            public void run() {
                                EmployeeThumb.setImageBitmap(bitmap);
                                EmployeeThumb.refreshDrawableState();
                            }
                        });


                        String log = "Capture Success\n" + "\nQuality: " + fingerData.Quality()
                                + "\nNFIQ: " + fingerData.Nfiq()
                                + "\nWSQ Compress Ratio: "
                                + fingerData.WSQCompressRatio()
                                + "\nImage Dimensions (inch): "
                                + fingerData.InWidth() + "\" X "
                                + fingerData.InHeight() + "\""
                                + "\nImage Area (inch): " + fingerData.InArea()
                                + "\"" + "\nResolution (dpi/ppi): "
                                + fingerData.Resolution() + "\nGray Scale: "
                                + fingerData.GrayScale() + "\nBits Per Pixal: "
                                + fingerData.Bpp() + "\nWSQ Info: "
                                + fingerData.WSQInfo();
                        SetLogOnUIThread(log);
                        SetToastUIThread(log);

                        //////////////////// Extract ANSI Template
                        byte[] tempData = new byte[2000]; // length 2000 is mandatory
                        byte[] ansiTemplate;
                        int dataLen = mfs100.ExtractANSITemplate(fingerData.RawData(), tempData);
                        if (dataLen <= 0) {
                            if (dataLen == 0) {
                                SetLogOnUIThread("Failed to extract ANSI Template");
                            } else {
                                SetLogOnUIThread(mfs100.GetErrorMsg(dataLen));
                            }
                            return;
                        } else {
                            ansiTemplate = new byte[dataLen];
                            System.arraycopy(tempData, 0, ansiTemplate, 0,
                                    dataLen);
                        }
                        //////////////////////////////////////////////

                        //////////////////// Extract ISO Image
                        dataLen = 0;
                        tempData = new byte[(mfs100.GetDeviceInfo().Width() * mfs100.GetDeviceInfo().Height()) + 1078];
                        byte[] isoImage = null;
                        dataLen = mfs100.ExtractISOImage(fingerData.RawData(), tempData);
                        if (dataLen <= 0) {
                            if (dataLen == 0) {
                                SetLogOnUIThread("Failed to extract ISO Image");
                            } else {
                                SetLogOnUIThread(mfs100.GetErrorMsg(dataLen));
                            }
                            return;
                        } else {
                            isoImage = new byte[dataLen];
                            System.arraycopy(tempData, 0, isoImage, 0,
                                    dataLen);
                        }
                        //////////////////////////////////////////////

                        //////////////////// Extract WSQ Image
                        dataLen = 0;
                        tempData = new byte[(mfs100.GetDeviceInfo().Width() * mfs100.GetDeviceInfo().Height()) + 1078];
                        byte[] wsqImage = null;
                        dataLen = mfs100.ExtractWSQImage(fingerData.RawData(), tempData);

                        if (dataLen <= 0) {
                            if (dataLen == 0) {
                                SetLogOnUIThread("Failed to extract WSQ Image");
                            } else {
                                SetLogOnUIThread(mfs100.GetErrorMsg(dataLen));
                            }
                            return;
                        } else {
                            wsqImage = new byte[dataLen];
                            System.arraycopy(tempData, 0, wsqImage, 0,
                                    dataLen);
                        }
                        //////////////////////////////////////////////

//                        SetData2(fingerData, ansiTemplate, isoImage, wsqImage);
                    }
                } catch (Exception ex) {
                    SetLogOnUIThread("Error");
                }
            }
        }).start();
    }

    private void UnInitScanner() {
        try {
            int ret = mfs100.UnInit();
            if (ret != 0) {
                SetLogOnUIThread(mfs100.GetErrorMsg(ret));
            } else {
                SetLogOnUIThread("Uninit Success");
                SetLogOnUIThread("Uninit Success");
            }
        } catch (Exception e) {
            Log.e("UnInitScanner.EX", e.toString());
        }
    }

    private void WriteFile(String filename, byte[] bytes) {
        try {
            String path = Environment.getExternalStorageDirectory()
                    + "//FingerData";
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            path = path + "//" + filename;
            file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream stream = new FileOutputStream(path);
            stream.write(bytes);
            stream.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void OnCaptureCompleted(boolean status, int errorCode,
                                   String errorMsg, FingerData fingerData) {
//		SetLogOnUIThread("EndTime: " + getCurrentTime());
        if (status) {
            final Bitmap bitmap = BitmapFactory.decodeByteArray(
                    fingerData.FingerImage(), 0,
                    fingerData.FingerImage().length);
            EmployeeThumb.post(new Runnable() {
                @Override
                public void run() {
                    EmployeeThumb.setImageBitmap(bitmap);
                    EmployeeThumb.refreshDrawableState();
                }
            });
            SetLogOnUIThread("Capture Success");
            String log = "\nQuality: " + fingerData.Quality() + "\nNFIQ: "
                    + fingerData.Nfiq() + "\nWSQ Compress Ratio: "
                    + fingerData.WSQCompressRatio()
                    + "\nImage Dimensions (inch): " + fingerData.InWidth()
                    + "\" X " + fingerData.InHeight() + "\""
                    + "\nImage Area (inch): " + fingerData.InArea() + "\""
                    + "\nResolution (dpi/ppi): " + fingerData.Resolution()
                    + "\nGray Scale: " + fingerData.GrayScale()
                    + "\nBits Per Pixal: " + fingerData.Bpp() + "\nWSQ Info: "
                    + fingerData.WSQInfo();
            SetLogOnUIThread(log);

            SetData(fingerData);
        } else {
            SetLogOnUIThread("Error: " + errorCode + "(" + errorMsg + ")");
        }
    }

    public void SetData(FingerData fingerData) {

        if (scannerAction.equals(CommonMethod.ScannerAction.Verify)) {
            Verify_Template = new byte[fingerData.ISOTemplate().length];
            System.arraycopy(fingerData.ISOTemplate(), 0, Verify_Template, 0,
                    fingerData.ISOTemplate().length);
        }

        Boolean isEmployeeAlreadyRegistered = false;
        EmployeeRegistered employeeRegistered = null;

        for (int i = 0; i < arrHavingAllRegisteredEmpDetails.size(); i++) {
            employeeRegistered = arrHavingAllRegisteredEmpDetails.get(i);


            byte[] Enroll_Template = employeeRegistered.getThumbbyte();//new byte[employeeRegistered.getThumbbyte().length];
//            System.arraycopy(employeeRegistered.getThumbbyte(), 0, Enroll_Template, 0,
//                    employeeRegistered.getThumbbyte().length);

            if (Enroll_Template == null) {
                SetLogOnUIThread("Enrolled template not found.");
                return;
            }
            if (Enroll_Template.length <= 0) {
                SetLogOnUIThread("Enrolled template not found.");
                return;
            }

            int ret = mfs100.MatchISO(Enroll_Template, Verify_Template);
            if (ret < 0) {
                SetLogOnUIThread("Error: " + ret + "("
                        + mfs100.GetErrorMsg(ret) + ")");
            } else {
                if (ret >= 1400) {
                    SetLogOnUIThread("Finger matched with score: " + ret);
                    isEmployeeAlreadyRegistered = true;
                    break;
                } else {
                    SetLogOnUIThread("Finger not matched, score: " + ret);
                }
            }
        }

        if (isEmployeeAlreadyRegistered) {
            SetLogOnUIThread("Finger matched with employeeID: " + employeeRegistered.employeeID);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(
                    employeeRegistered.getProfilebyte(), 0, employeeRegistered.getProfilebyte().length);
            Employeeprofile.post(new Runnable() {
                @Override
                public void run() {
                    Employeeprofile.setImageBitmap(bitmap);
                    Employeeprofile.refreshDrawableState();
                }
            });

            Calendar c = Calendar.getInstance();

            SetLogOnUIThread("Current time 346--" + c.getTime());

            SimpleDateFormat df1 = new SimpleDateFormat("dd-MM-yy");
            SetLogOnUIThread("Current time 5645--" + c.getTime());
            String formattedDate = df1.format(c.getTime());
            SetLogOnUIThread("Current time 1567561--" + c.getTime());

            SimpleDateFormat df2 = new SimpleDateFormat("HH:mm");
            SetLogOnUIThread("Current time 6657--" + c.getTime());
            String formattedTime = df2.format(c.getTime());
            SetLogOnUIThread("Current time 77--" + c.getTime());

            SimpleDateFormat df3 = new SimpleDateFormat("EEEE");
            SetLogOnUIThread("Current time 1881--" + c.getTime());
            String formattedDay = df3.format(c.getTime());
            SetLogOnUIThread("Current time 22--" + c.getTime());

            String ATTENDANCE_TABLE = "attendace_table";
            String ID = "id";
            String DATE = "date";
            String OUT_TIME = "out_time";
            String arrr = "update " + ATTENDANCE_TABLE + " SET " + OUT_TIME + " = '" + formattedTime + "' where " + ID + " = " + employeeRegistered.employeeID + " and " + DATE + " = '" + formattedDate + "'";
            SetLogOnUIThread("arrrarrrarrr--" + arrr);
            Cursor cursor = employeeDatabaseHelper.isEmployeeAlreadyPunchedInADay(employeeRegistered.employeeID, formattedDate);
            SetLogOnUIThread("Current time 33--" + c.getTime());
            if (cursor != null && cursor.getCount() > 0) {
                SetLogOnUIThread("Current time 44--" + cursor.getCount());
                int updatedRows = employeeDatabaseHelper.updateEmployeeOutTime(employeeRegistered.employeeID, formattedDate, formattedTime);
                SetLogOnUIThread("updatedRows--" + updatedRows);

                SetToastUIThread("Attendance Saved. Thanks");
                playBeep("thankyou.mp3");
                finish();
                Intent in = new Intent(DailyAttendanceActivity.this, HomeActivity.class);
                in.putExtra("data", "attendance");
                startActivity(in);
            } else {
                SetLogOnUIThread("Current time addAttendance--");
                employeeDatabaseHelper.addAttendance(employeeRegistered.employeeID, employeeRegistered.getEmployeeName(), formattedDate, formattedDay, formattedTime, "");

                playBeep("thankyou.mp3");
                SetToastUIThread("Attendance Saved. Thanks");

                finish();
                Intent in = new Intent(DailyAttendanceActivity.this, HomeActivity.class);
                in.putExtra("data", "attendance");
                startActivity(in);
            }


        } else {
            playBeep("pressagain.mp3");
        }


//        WriteFile("Raw.raw", fingerData.RawData());
//        WriteFile("Bitmap.bmp", fingerData.FingerImage());
//        WriteFile("ISOTemplate.iso", fingerData.ISOTemplate());
//        WriteFile("ANSITemplate.ansi", fingerData.ANSITemplate());
//        WriteFile("ISOImage.iso", fingerData.ISOImage());
//        WriteFile("WSQ.wsq", fingerData.WSQImage());

    }

//    public void SetData2(FingerData fingerData, byte[] ANSITemplate, byte[] IsoImage, byte[] WsqImage) {
//        if (scannerAction.equals(CommonMethod.ScannerAction.Capture)) {
//            Enroll_Template = new byte[fingerData.ISOTemplate().length];
//            System.arraycopy(fingerData.ISOTemplate(), 0, Enroll_Template, 0,
//                    fingerData.ISOTemplate().length);
//        } else if (scannerAction.equals(CommonMethod.ScannerAction.Verify)) {
//            Verify_Template = new byte[fingerData.ISOTemplate().length];
//            System.arraycopy(fingerData.ISOTemplate(), 0, Verify_Template, 0,
//                    fingerData.ISOTemplate().length);
//            int ret = mfs100.MatchISO(Enroll_Template, Verify_Template);
//            if (ret < 0) {
//                SetLogOnUIThread("Error: " + ret + "("
//                        + mfs100.GetErrorMsg(ret) + ")");
//            } else {
//                if (ret >= 1400) {
//                    SetLogOnUIThread("Finger matched with score: " + ret);
//                } else {
//                    SetLogOnUIThread("Finger not matched, score: " + ret);
//                }
//            }
//        }
//
//        WriteFile("Raw.raw", fingerData.RawData());
//        WriteFile("Bitmap.bmp", fingerData.FingerImage());
//        WriteFile("ISOTemplate.iso", fingerData.ISOTemplate());
//        WriteFile("ANSITemplate.ansi", ANSITemplate);
//        WriteFile("ISOImage.iso", IsoImage);
//        WriteFile("WSQ.wsq", WsqImage);
//
//    }

    public void playBeep(String fileName) {

        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = new MediaPlayer();
            }

            AssetFileDescriptor descriptor = getAssets().openFd(fileName);
            mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();

            mediaPlayer.prepare();
            mediaPlayer.setVolume(1f, 1f);
            mediaPlayer.setLooping(false);
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void OnDeviceAttached(int vid, int pid, boolean hasPermission) {
        int ret = 0;
        if (!hasPermission) {
            SetLogOnUIThread("Permission denied");
            return;
        }
        if (vid == 1204 || vid == 11279) {
            if (pid == 34323) {
                ret = mfs100.LoadFirmware();
                if (ret != 0) {
                    SetLogOnUIThread(mfs100.GetErrorMsg(ret));
                } else {
                    SetLogOnUIThread("Loadfirmware success");
                    isConnectedDevice = true;
                }
            } else if (pid == 4101) {

                String key = "Without Key";
                ret = mfs100.Init("");
                if (ret == -1322) {
                    key = "Test Key";
                    ret = mfs100.Init(_testKey);
                }

                if (ret == 0) {
                    showSuccessLog(key);
                    isConnectedDevice = true;
                } else {
                    SetLogOnUIThread(mfs100.GetErrorMsg(ret));
                }

            }
        }
    }

    @Override
    public void OnPreview(FingerData fingerData) {
        final Bitmap bitmap = BitmapFactory.decodeByteArray(
                fingerData.FingerImage(), 0, fingerData.FingerImage().length);
        EmployeeThumb.post(new Runnable() {
            @Override
            public void run() {
                EmployeeThumb.setImageBitmap(bitmap);
                EmployeeThumb.refreshDrawableState();

                mthumb = bitmap;

            }
        });
        // Log.e("OnPreview.Quality", String.valueOf(fingerData.Quality()));
        SetLogOnUIThread("Quality: " + fingerData.Quality());
    }

    private void showSuccessLog(String key) {
        SetLogOnUIThread("Init success");
        String info = "\nKey: " + key + "\nSerial: "
                + mfs100.GetDeviceInfo().SerialNo() + " Make: "
                + mfs100.GetDeviceInfo().Make() + " Model: "
                + mfs100.GetDeviceInfo().Model()
                + "\nCertificate: " + mfs100.GetCertification();
        SetLogOnUIThread(info);
    }

    @Override
    public void OnDeviceDetached() {
        UnInitScanner();
        isConnectedDevice = false;
        SetLogOnUIThread("Device removed");
    }

    @Override
    public void OnHostCheckFailed(String err) {
        try {
            SetLogOnUIThread(err);
            Toast.makeText(this, err, Toast.LENGTH_LONG).show();
        } catch (Exception ex) {

        }
    }

    public void open() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please Connect FingerPrint Device")
                .setCancelable(false)
                .setTitle("Alert")
                .setIcon(R.drawable.alerticon)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });


        AlertDialog alert = builder.create();

        alert.setTitle("Alert");
        alert.show();
    }

}

