package com.example.android1.hrms.modelclass;

/**
 * Created by Android1 on 7/17/2017.
 */

public class EmployeeRegistered {

    public int employeeID;
    String employeeName;
    byte[] thumbbyte, profilebyte;

    public EmployeeRegistered(int employeeID, String employeeName, byte[] thumbbyte, byte[] profilebyte) {
        this.employeeID = employeeID;
        this.employeeName = employeeName;
        this.thumbbyte = thumbbyte;
        this.profilebyte = profilebyte;
    }

    public int getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(int employeeID) {
        this.employeeID = employeeID;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public byte[] getThumbbyte() {
        return thumbbyte;
    }

    public void setThumbbyte(byte[] thumbbyte) {
        this.thumbbyte = thumbbyte;
    }

    public byte[] getProfilebyte() {
        return profilebyte;
    }

    public void setProfilebyte(byte[] profilebyte) {
        this.profilebyte = profilebyte;
    }
}
