package com.example.android1.hrms.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android1.hrms.modelclass.EmployeeDataModelClass;
import com.example.android1.hrms.R;

import java.util.ArrayList;

/**
 * Created by Android1 on 7/17/2017.
 */

public class EmployeeAdapter extends BaseAdapter {


    ArrayList<EmployeeDataModelClass> employeeList = new ArrayList<>();

    public EmployeeAdapter(ArrayList<EmployeeDataModelClass> employeeList) {
        this.employeeList = employeeList;
    }


    @Override
    public int getCount() {
        return employeeList.size();
    }

    @Override
    public Object getItem(int i) {
        return employeeList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null)
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.employee_item_layout, viewGroup, false);

        TextView email = (TextView) view.findViewById(R.id.emailid);
        ImageView profile = (ImageView) view.findViewById(R.id.profilepic);
        TextView name = (TextView) view.findViewById(R.id.name);
        TextView mobile = (TextView) view.findViewById(R.id.mobileno);
        name.setText(employeeList.get(i).getEmployeeName());
        email.setText(employeeList.get(i).getEmployeeEmail());
        mobile.setText(employeeList.get(i).getEmployeeMobile());
        Bitmap profileBitmap = BitmapFactory.decodeByteArray(employeeList.get(i).getProfilebyte(), 0, employeeList.get(i).getProfilebyte().length);

        profile.setImageBitmap(profileBitmap);


        return view;
    }
}
