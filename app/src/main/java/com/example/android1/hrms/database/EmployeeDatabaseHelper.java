package com.example.android1.hrms.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class EmployeeDatabaseHelper extends SQLiteOpenHelper {

    static String DATABASE_NAME = "employee_database";

    //////          employee table            ///////
    static String EMPLOYEE_TABLE = "employee_table";
    static String ID = "id";
    static String NAME = "name";
    static String EMAIL = "email";
    static String PHONE = "phone";
    static String PIC = "pic";
    static String Thumb = "thumb";
    static String AADHARNO = "aadharno";

    static String CREATE_EMPLOYEE_TABLE =
            "CREATE TABLE " + EMPLOYEE_TABLE + "("
                    + ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                    + NAME + " TEXT,"
                    + EMAIL + " TEXT,"
                    + PHONE + " TEXT,"
                    + AADHARNO + " TEXT,"
                    + PIC + " BLOB,"
                    + Thumb + " BLOB);";

    //////      attendence table            /////////
    static String ATTENDANCE_TABLE = "attendace_table";
    static String DATE = "date";
    static String DAY = "day";
    static String IN_TIME = "in_time";
    static String OUT_TIME = "out_time";

    static String CREATE_ATTENDANCE_TABLE =
            "CREATE TABLE " + ATTENDANCE_TABLE + "("
                    + ID + " INTEGER,"
                    + NAME + " TEXT,"
                    + DATE + " TEXT,"
                    + DAY + " TEXT,"
                    + IN_TIME + " TEXT,"
                    + OUT_TIME + " TEXT);";


    public EmployeeDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_EMPLOYEE_TABLE);
        sqLiteDatabase.execSQL(CREATE_ATTENDANCE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE " + EMPLOYEE_TABLE + " IF EXISTS");
        sqLiteDatabase.execSQL("DROP TABLE " + ATTENDANCE_TABLE + " IF EXISTS");
        onCreate(sqLiteDatabase);
    }

    public boolean addEmployee(String name_, String email_, String phone_,String aadhar_, byte[] pic, byte[] thumb) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(NAME, name_);
        values.put(EMAIL, email_);
        values.put(PHONE, phone_);
        values.put(AADHARNO,aadhar_);
        values.put(PIC, pic);
        values.put(Thumb, thumb);
        long r = db.insert(EMPLOYEE_TABLE, null, values);
        return r != -1;
    }

    public Cursor getEmployee(int id_) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select * from " + EMPLOYEE_TABLE + " where id='" + id_ + "'", null);
        return c;
    }

    public Cursor getAllEmployee() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select * from " + EMPLOYEE_TABLE, null);
        return c;
    }


//    void updateEmployeeOutTime(int EmpId,String strDate,String outTime) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor c = db.rawQuery("update "+ATTENDANCE_TABLE+" SET "+OUT_TIME+" = '"+outTime+"' where " +ID+ " = "+EmpId+" and " +DATE+ " = '"+strDate+"'", null);
//
//    }

    public int updateEmployeeOutTime(int EmpId, String strDate, String outTime) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(OUT_TIME, outTime);

        // updating row
        return db.update(ATTENDANCE_TABLE, values, ID + " = ? AND " + DATE + " = ?",
                new String[]{String.valueOf(EmpId), strDate});
    }

    public Cursor isEmployeeAlreadyPunchedInADay(int EmpId, String strDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select * from " + ATTENDANCE_TABLE + " where " + ID + " = " + EmpId + " and " + DATE + " = '" + strDate + "'", null);
        return c;
    }

    public boolean addAttendance(int id_, String name_, String date_, String day_, String in_time, String out_time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(ID, id_);
        values.put(NAME, name_);
        values.put(DATE, date_);
        values.put(DAY, day_);
        values.put(IN_TIME, in_time);
        values.put(OUT_TIME, out_time);

        long r = db.insert(ATTENDANCE_TABLE, null, values);
        return r != -1;
    }

    public Cursor getAttendance() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select * from " + ATTENDANCE_TABLE, null);
        return c;
    }
}




