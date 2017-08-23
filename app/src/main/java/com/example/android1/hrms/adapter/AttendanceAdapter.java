package com.example.android1.hrms.adapter;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.android1.hrms.modelclass.AttendanceItem;
import com.example.android1.hrms.R;

import java.util.ArrayList;

public class AttendanceAdapter extends BaseAdapter {



    ArrayList<AttendanceItem> AttendanceItemList =new ArrayList<>();
    public AttendanceAdapter(ArrayList<AttendanceItem> AttendanceItemList) {
                this.AttendanceItemList=AttendanceItemList;
    }


    @Override
    public int getCount() {
        return AttendanceItemList.size();
    }

    @Override
    public Object getItem(int i) {
        return AttendanceItemList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null)
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.attendanceitem_layout,viewGroup,false);

        TextView employee_name = (TextView)view.findViewById(R.id.employee_name);
        TextView date_text = (TextView)view.findViewById(R.id.Date);
        TextView day_text = (TextView)view.findViewById(R.id.day);
        TextView in_text = (TextView)view.findViewById(R.id.InTime);
        TextView out_text = (TextView)view.findViewById(R.id.OutTime);

        employee_name.setText(AttendanceItemList.get(i).getName());
        date_text.setText(AttendanceItemList.get(i).getDate());
        day_text.setText(AttendanceItemList.get(i).getDay());
        in_text.setText(AttendanceItemList.get(i).getInTime());
        Log.d("time",AttendanceItemList.get(i).getOutTime());
        out_text.setText(AttendanceItemList.get(i).getOutTime());


        return view;
    }
}
