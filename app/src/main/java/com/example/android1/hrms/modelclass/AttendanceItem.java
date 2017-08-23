package com.example.android1.hrms.modelclass;

/**
 * Created by Android1 on 7/17/2017.
 */

public class AttendanceItem {
    String Name,Date,InTime,OutTime,Day;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public AttendanceItem(String Name,String Date, String Day, String InTime, String OutTime)
    {

        this.Name = Name;
    this.Date=Date;
        this.Day=Day;
        this.InTime=InTime;
        this.OutTime=OutTime;
    }



    public String getDate() {
        return Date;
    }

    public String getDay() {
        return Day;
    }

    public String getInTime() {
        return InTime;
    }

    public String getOutTime() {
        return OutTime;
    }
}
