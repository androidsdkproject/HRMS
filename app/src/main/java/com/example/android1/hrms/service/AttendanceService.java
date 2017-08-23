package com.example.android1.hrms.service;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.android1.hrms.ShowAttendanceServiceActivity;
import com.example.android1.hrms.Util.CommonMethod;
import com.example.android1.hrms.database.EmployeeDatabaseHelper;
import com.example.android1.hrms.modelclass.EmployeeRegistered;
import com.example.android1.hrms.receiver.FingerPrintReceiver;
import com.mantra.mfs100.FingerData;
import com.mantra.mfs100.MFS100;
import com.mantra.mfs100.MFS100Event;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class AttendanceService extends Service implements MFS100Event {

    public static String _testKey = "t7L8wTG/iv02t+pgYrMQ7tt8qvU1z42nXpJDfAfsW592N4sKUHLd8A0MEV0GRxH+f4RgefEaMZALj7mgm/Thc0jNhR2CW9BZCTgeDPjC6q0W";
    private final BroadcastReceiver fingerPrintReceiver = new FingerPrintReceiver();
    byte[] Verify_Template;
    int mfsVer = 41;
    int minQuality = 60;
    int timeout = 1000000;
    boolean attendance_status = false;
    MFS100 mfs100 = null;
    Context context;
    SharedPreferences settings;
    CommonMethod.ScannerAction scannerAction = CommonMethod.ScannerAction.Capture;
    EmployeeDatabaseHelper employeeDatabaseHelper;
    AlarmManager alarmManager;
    Intent fingerPrintIntent;
    PendingIntent pendingIntent;
    ArrayList<EmployeeRegistered> arrHavingAllRegisteredEmpDetails = new ArrayList<>();
    MediaPlayer mediaPlayer = null;
    String TAG = "service";

    @Override
    public void onCreate() {

        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "1 on start command service");

        ////init ui

        attendance_status = false;
        employeeDatabaseHelper = new EmployeeDatabaseHelper(this);
        mediaPlayer = new MediaPlayer();

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        context = this.getApplicationContext();
        mfsVer = Integer.parseInt(settings.getString("MFSVer",
                String.valueOf(mfsVer)));

        CommonMethod.DeleteDirectory();
        CommonMethod.CreateDirectory();

        mfs100 = new MFS100(this, mfsVer);
        try {
            mfs100.SetApplicationContext(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

//


        doMatch();
        InitScanner();
        scannerAction = CommonMethod.ScannerAction.Verify;
        StartSyncCapture();
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (null != mfs100)
                mfs100.Dispose();
            Log.d(TAG, "service Destroy");
//            stopSelf();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
//            }
//        }).start();
    }


    private void StopAsynCapture() {
        mfs100.StopCapture();
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
//            if(count==0) {
//                Toast.makeText(this, "kamal", Toast.LENGTH_LONG).show();
//                count++;
//            }
//            else {
//                Toast.makeText(this, count+" balveer", Toast.LENGTH_LONG).show();
//            }

            SetData(fingerData);
        } else {
            SetLogOnUIThread("Error: " + errorCode + "(" + errorMsg + ")");
        }
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


                        SetLogOnUIThread("Capture Success");
                        String log = "\nQuality: " + fingerData.Quality()
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


                        SetData(fingerData);
                    }
                } catch (Exception ex) {
                    SetLogOnUIThread("Error");
                }
            }
        }).start();
    }


    synchronized public void SetData(FingerData fingerData) {

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

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        if (!cn.getClassName().equalsIgnoreCase("com.example.android1.hrms.RegistrationEmployeeActivity") && !cn.getClassName().equalsIgnoreCase("com.example.android1.hrms.DailyAttendanceActivity")) {

            if (isEmployeeAlreadyRegistered) {
                SetLogOnUIThread("Finger matched with employeeID: " + employeeRegistered.employeeID);
                final Bitmap bitmap = BitmapFactory.decodeByteArray(
                        employeeRegistered.getProfilebyte(), 0, employeeRegistered.getProfilebyte().length);


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
                if (cursor != null && cursor.getCount() > 0 && !attendance_status) {
                    attendance_status = true;
                    SetLogOnUIThread("Current time 44--" + cursor.getCount());
                    int updatedRows = 0;
                    try {
                        updatedRows = employeeDatabaseHelper.updateEmployeeOutTime(employeeRegistered.employeeID, formattedDate, formattedTime);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    SetLogOnUIThread("updatedRows--" + updatedRows);
                    playBeep("thankyou.mp3");
                    stopSelf();
                    Intent serviceintent = new Intent(this, ShowAttendanceServiceActivity.class);
                    serviceintent.putExtra("id", employeeRegistered.employeeID);
                    serviceintent.putExtra("test", 1);
                    serviceintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    serviceintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(serviceintent);


                } else if (!attendance_status) {
                    attendance_status = true;
                    SetLogOnUIThread("Current time addAttendance--");

                    try {
                        employeeDatabaseHelper.addAttendance(employeeRegistered.employeeID, employeeRegistered.getEmployeeName(), formattedDate, formattedDay, formattedTime, "");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    playBeep("thankyou.mp3");
                    stopSelf();
                    Intent serviceintent = new Intent(this, ShowAttendanceServiceActivity.class);
                    serviceintent.putExtra("id", employeeRegistered.employeeID);
                    serviceintent.putExtra("test", 2);
                    serviceintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    serviceintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(serviceintent);


                }


            } else {
                playBeep("pressagain.mp3");
                stopSelf();
                startService(new Intent(AttendanceService.this, AttendanceService.class));
            }

        }


//        WriteFile("Raw.raw", fingerData.RawData());
//        WriteFile("Bitmap.bmp", fingerData.FingerImage());
//        WriteFile("ISOTemplate.iso", fingerData.ISOTemplate());
//        WriteFile("ANSITemplate.ansi", fingerData.ANSITemplate());
//        WriteFile("ISOImage.iso", fingerData.ISOImage());
//        WriteFile("WSQ.wsq", fingerData.WSQImage());

    }


    private void startServiceReceiver() {
        Log.d(TAG, "startAlarmManager");


        Context context = getBaseContext();
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        fingerPrintIntent = new Intent(context, FingerPrintReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(context, 0, fingerPrintIntent, 0);

        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                1 * 1000, // 60000 = 1 minute
                pendingIntent);

    }


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
                }
                scannerAction = CommonMethod.ScannerAction.Verify;
                StartAsyncCapture();
            } else if (pid == 4101) {

                String key = "Without Key";
                ret = mfs100.Init("");
                if (ret == -1322) {
                    key = "Test Key";
                    ret = mfs100.Init(_testKey);
                }

                if (ret == 0) {
                    showSuccessLog(key);
                } else {
                    SetLogOnUIThread(mfs100.GetErrorMsg(ret));
                }

                scannerAction = CommonMethod.ScannerAction.Verify;
                StartAsyncCapture();

            }
        }
    }

    @Override
    public void OnPreview(FingerData fingerData) {
        final Bitmap bitmap = BitmapFactory.decodeByteArray(
                fingerData.FingerImage(), 0, fingerData.FingerImage().length);

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
        scannerAction = CommonMethod.ScannerAction.Verify;
        StartAsyncCapture();
    }

    @Override
    public void OnDeviceDetached() {
        UnInitScanner();
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


}