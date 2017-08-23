package com.example.android1.hrms;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.android1.hrms.Util.CommonMethod;
import com.example.android1.hrms.database.EmployeeDatabaseHelper;
import com.example.android1.hrms.service.AttendanceService;
import com.mantra.mfs100.FingerData;
import com.mantra.mfs100.MFS100;
import com.mantra.mfs100.MFS100Event;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
public class RegistrationEmployeeActivity extends AppCompatActivity implements View.OnClickListener, MFS100Event {
    private static final int CAMERA_REQUEST = 1888;
    public static String _testKey = "t7L8wTG/iv02t+pgYrMQ7tt8qvU1z42nXpJDfAfsW592N4sKUHLd8A0MEV0GRxH+f4RgefEaMZALj7mgm/Thc0jNhR2CW9BZCTgeDPjC6q0W";
    byte[] Enroll_Template;
    byte[] Verify_Template;
    int mfsVer = 41;
    String TAG = "RegistrationEmployee";
    int minQuality = 60;
    int timeout = 1000000;
    MFS100 mfs100 = null;


    RelativeLayout registrationlayout;
    TextView logs;
    Context context;
    TextView textViewQuality;
    EditText editTextemail, editTextMobile, editTextname, editAadharCardNo;
    ImageView Employeeprofile, EmployeeThumb;
    Button submitbutton;
    Bitmap mphoto;
    Bitmap mthumb;


    SharedPreferences settings;
    MediaPlayer mediaPlayer = null;
    boolean isConnectedDevice = false;
    CommonMethod.ScannerAction scannerAction = CommonMethod.ScannerAction.Capture;
    EmployeeDatabaseHelper employeeDatabaseHelper;


    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public final static boolean isValidPhone(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            int len = target.length();
            if (len == 10)
                return true;
            else
                return false;
        }
    }

    public void stopService() {

        Log.d(TAG, "stop service");
        stopService(new Intent(getBaseContext(), AttendanceService.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_employee);
        employeeDatabaseHelper = new EmployeeDatabaseHelper(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);


            getSupportActionBar().setTitle("Registration");

        }
        stopService();
        initUI();
        InitScanner();


    }

    private void initUI() {

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        context = RegistrationEmployeeActivity.this.getApplicationContext();
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


        ///Edit TExt
        editTextname = (EditText) findViewById(R.id.editemployeename);
        editTextMobile = (EditText) findViewById(R.id.editmobilenumber);
        editTextemail = (EditText) findViewById(R.id.editemailid);
        editAadharCardNo = (EditText) findViewById(R.id.editaadharcard);
        logs = (TextView) findViewById(R.id.logs);
        registrationlayout = (RelativeLayout) findViewById(R.id.registrationlayout);
        ///ImageView
        Employeeprofile = (ImageView) findViewById(R.id.EmployeePic);
        EmployeeThumb = (ImageView) findViewById(R.id.EmployeeThumb);
        textViewQuality = (TextView) findViewById(R.id.quality);


        submitbutton = (Button) findViewById(R.id.submit);
        submitbutton.setOnClickListener(this);

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
            case R.id.submit:
                addEmployee();
                break;


            case R.id.EmployeePic:
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editAadharCardNo.getWindowToken(),
                        InputMethodManager.RESULT_UNCHANGED_SHOWN);
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
                break;

            case R.id.EmployeeThumb:
                stopService();
                if (isConnectedDevice) {
                    EmployeeThumb.clearColorFilter();
                    textViewQuality.setText("");
                    scannerAction = CommonMethod.ScannerAction.Capture;
                    StartAsyncCapture();

                } else {
                    open();
                }


                break;
        }
    }

    boolean isValidAadharCard(String AadharCard) {
        if (AadharCard.length() == 12)
            return true;
        else
            return false;

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


        alert.show();
    }

    private void addEmployee() {
        String name, mobile, email, aadharcardno;
        email = editTextemail.getText().toString();
        mobile = editTextMobile.getText().toString();
        name = editTextname.getText().toString();
        aadharcardno = editAadharCardNo.getText().toString();

        if (name.isEmpty()) {
            editTextname.setError("name can not be blank");
        } else if (!isValidPhone(mobile)) {
            editTextMobile.setError("enter valid mobile no");
        } else if (!isValidEmail(email)) {
            editTextemail.setError("enter valid email");
        } else if (!isValidAadharCard(aadharcardno)) {
            editAadharCardNo.setError("enter valid aadhar card");
        } else if (mphoto == null) {

            SnackBarMethod(0, false, "Capture Employee Image");
            ///Toast.makeText(getApplicationContext(), "capture employee image", Toast.LENGTH_LONG).show();
        } else if (Enroll_Template == null) {
            SnackBarMethod(0, false, "Capture Thumb Expression");
            ///Toast.makeText(getApplicationContext(), "capture thumb expression", Toast.LENGTH_LONG).show();
        } else {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            mphoto.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteImage = stream.toByteArray();


            if (employeeDatabaseHelper.addEmployee(name, email, mobile, aadharcardno, byteImage, Enroll_Template)) {
                ///Toast.makeText(getApplicationContext(), "employee successfully registered", Toast.LENGTH_LONG).show();
                ///SnackBarMethod(1, true, "Employee Successfully Registered");
                regSuccessAlert();
            } else {
                SnackBarMethod(1, false, "Employee not Registered");
                ///Toast.makeText(getApplicationContext(), "employee not registered", Toast.LENGTH_LONG).show();
            }
        }
    }


    void SnackBarMethod(int type, boolean status, String msg) {
        final Snackbar snackbar = Snackbar.make(registrationlayout, msg, Snackbar.LENGTH_LONG);
        View view = snackbar.getView();
        view.getLayoutParams().width = AppBarLayout.LayoutParams.MATCH_PARENT;

        if (type == 0) {
            if (status) {
                TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                tv.setTextSize(20);
                tv.setTextColor(Color.GREEN);
                snackbar.show();
            } else {
                TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                tv.setTextSize(20);
                tv.setTextColor(Color.RED);
                snackbar.show();
            }

        } else if (type == 1) {
            if (status) {
                TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                tv.setTextColor(Color.GREEN);
                tv.setTextSize(20);
                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.setAction("ok", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent in = new Intent(RegistrationEmployeeActivity.this, HomeActivity.class);
                        in.putExtra("data", "reg");
                        startActivity(in);
                        finish();
                    }
                });
                snackbar.show();
            } else {
                TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                tv.setTextColor(Color.RED);
                tv.setTextSize(20);
                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.setAction("Try Again", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                    }
                });
                snackbar.show();
            }
        }


    }

    void regSuccessAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Employee successfully registered")
                .setCancelable(false)
                .setIcon(R.mipmap.tick)
                .setTitle("Success")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();

                        Intent in = new Intent(RegistrationEmployeeActivity.this, HomeActivity.class);
                        in.putExtra("data", "reg");
                        startActivity(in);
                        finish();
                    }
                });


        AlertDialog alert = builder.create();


        alert.show();
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            //Toast.makeText(getApplicationContext(), "capture", Toast.LENGTH_LONG).show();
            mphoto = (Bitmap) data.getExtras().get("data");
            Employeeprofile.setImageBitmap(mphoto);
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
//                logs.append(str);
                CommonMethod.writeLog(context, "\n"+str);
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

                        SetData2(fingerData, ansiTemplate, isoImage, wsqImage);
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
                                   String errorMsg, final FingerData fingerData) {
        if (status) {
            try {
                final Bitmap bitmap = BitmapFactory.decodeByteArray(
                        fingerData.FingerImage(), 0,
                        fingerData.FingerImage().length);
                EmployeeThumb.post(new Runnable() {
                    @Override
                    public void run() {
                        EmployeeThumb.setImageBitmap(bitmap);
                        EmployeeThumb.setColorFilter(Color.argb(50, 0, 255, 0));
                        EmployeeThumb.refreshDrawableState();
                    }
                });


                SetLogOnUIThread("Capture Success");


                textViewQuality.post(new Runnable() {
                    public void run() {
                        textViewQuality.setText("Quality " + fingerData.Quality() + "%");
                    }
                });


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
            } catch (Exception e) {
                SetLogOnUIThread("success my exception " + e.getMessage());
            }


        } else {
            SetLogOnUIThread("Error: " + errorCode + "(" + errorMsg + ")");
        }
    }

    public void SetData(FingerData fingerData) {
        if (scannerAction.equals(CommonMethod.ScannerAction.Capture)) {
            Enroll_Template = new byte[fingerData.ISOTemplate().length];
            System.arraycopy(fingerData.ISOTemplate(), 0, Enroll_Template, 0,
                    fingerData.ISOTemplate().length);

            if (null == Enroll_Template)
                SetLogOnUIThread("\nEnroll Template null\n");

        } else if (scannerAction.equals(CommonMethod.ScannerAction.Verify)) {
            if (Enroll_Template == null) {
                SetLogOnUIThread("Enrolled template not found.");
                return;
            }
            if (Enroll_Template.length <= 0) {
                SetLogOnUIThread("Enrolled template not found.");
                return;
            }
            Verify_Template = new byte[fingerData.ISOTemplate().length];
            System.arraycopy(fingerData.ISOTemplate(), 0, Verify_Template, 0,
                    fingerData.ISOTemplate().length);
            int ret = mfs100.MatchISO(Enroll_Template, Verify_Template);
            if (ret < 0) {
                SetLogOnUIThread("Error: " + ret + "("
                        + mfs100.GetErrorMsg(ret) + ")");
            } else {
                if (ret >= 1400) {
                    SetLogOnUIThread("Finger matched with score: " + ret);
                } else {
                    SetLogOnUIThread("Finger not matched, score: " + ret);
                }
            }
        }

        WriteFile("Raw.raw", fingerData.RawData());
        WriteFile("Bitmap.bmp", fingerData.FingerImage());
        WriteFile("ISOTemplate.iso", fingerData.ISOTemplate());
        WriteFile("ANSITemplate.ansi", fingerData.ANSITemplate());
        WriteFile("ISOImage.iso", fingerData.ISOImage());
        WriteFile("WSQ.wsq", fingerData.WSQImage());

    }

    public void SetData2(FingerData fingerData, byte[] ANSITemplate, byte[] IsoImage, byte[] WsqImage) {
        if (scannerAction.equals(CommonMethod.ScannerAction.Capture)) {
            Enroll_Template = new byte[fingerData.ISOTemplate().length];
            System.arraycopy(fingerData.ISOTemplate(), 0, Enroll_Template, 0,
                    fingerData.ISOTemplate().length);
        } else if (scannerAction.equals(CommonMethod.ScannerAction.Verify)) {
            Verify_Template = new byte[fingerData.ISOTemplate().length];
            System.arraycopy(fingerData.ISOTemplate(), 0, Verify_Template, 0,
                    fingerData.ISOTemplate().length);
            int ret = mfs100.MatchISO(Enroll_Template, Verify_Template);
            if (ret < 0) {
                SetLogOnUIThread("Error: " + ret + "("
                        + mfs100.GetErrorMsg(ret) + ")");
            } else {
                if (ret >= 1400) {
                    SetLogOnUIThread("Finger matched with score: " + ret);
                } else {
                    SetLogOnUIThread("Finger not matched, score: " + ret);
                }
            }
        }

        WriteFile("Raw.raw", fingerData.RawData());
        WriteFile("Bitmap.bmp", fingerData.FingerImage());
        WriteFile("ISOTemplate.iso", fingerData.ISOTemplate());
        WriteFile("ANSITemplate.ansi", ANSITemplate);
        WriteFile("ISOImage.iso", IsoImage);
        WriteFile("WSQ.wsq", WsqImage);

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
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


            stopService(new Intent(RegistrationEmployeeActivity.this, AttendanceService.class));
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }
}
