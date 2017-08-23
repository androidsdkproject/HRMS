package com.example.android1.hrms.modelclass;

/**
 * Created by Android1 on 7/19/2017.
 */

public class EmployeeDataModelClass {
    String employeeName, employeeMobile, employeeEmail, employeeAadharno;
    byte[] thumbbyte, profilebyte;

    public EmployeeDataModelClass(String employeeName, String employeeEmail, String employeeMobile, String employeeAadharno, byte[] profilebyte, byte[] thumbbyte) {
        this.employeeName = employeeName;
        this.employeeMobile = employeeMobile;
        this.employeeEmail = employeeEmail;
        this.employeeAadharno = employeeAadharno;
        this.thumbbyte = thumbbyte;
        this.profilebyte = profilebyte;

    }

    public String getEmployeeAadharno() {
        return employeeAadharno;
    }

    public void setEmployeeAadharno(String employeeAadharno) {
        this.employeeAadharno = employeeAadharno;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeMobile() {
        return employeeMobile;
    }

    public void setEmployeeMobile(String employeeMobile) {
        this.employeeMobile = employeeMobile;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
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
