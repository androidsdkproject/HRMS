package com.example.android1.hrms.fragment;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArraySet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;

import com.example.android1.hrms.adapter.AttendanceAdapter;
import com.example.android1.hrms.DailyAttendanceActivity;
import com.example.android1.hrms.modelclass.AttendanceItem;
import com.example.android1.hrms.R;
import com.example.android1.hrms.database.EmployeeDatabaseHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class AttendanceShowListFragment extends Fragment {

    EmployeeDatabaseHelper employeeDatabaseHelper;
    ArrayList<AttendanceItem> attendanceItemsList;
    ListView listView;
    ArraySet<String> EmployeeNameList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attendance_show_list, container, false);


        listView = (ListView) view.findViewById(R.id.listview);
        attendanceItemsList = new ArrayList<>();
        EmployeeNameList = new ArraySet<>();

        employeeDatabaseHelper = new EmployeeDatabaseHelper(getActivity());


        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fabattendance);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), DailyAttendanceActivity.class);
                startActivity(intent);
            }
        });


        Cursor c = employeeDatabaseHelper.getAttendance();

        if (c != null) {
            while (c.moveToNext()) {

                EmployeeNameList.add(c.getString(1));
                attendanceItemsList.add(new AttendanceItem(c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5)));
            }
        }

        AttendanceAdapter attendanceAdapter = new AttendanceAdapter(attendanceItemsList);
        listView.setAdapter(attendanceAdapter);


        ////////           filter by date       /////////

        ArrayList<String> employeeNameList = new ArrayList<>();

        for (String data : EmployeeNameList) {
            employeeNameList.add(data);
        }

        ArrayAdapter<String> adapterAutoCompleteTextView = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, employeeNameList);

        final AutoCompleteTextView employee_input = (AutoCompleteTextView) view.findViewById(R.id.employee);
        final EditText startDate = (EditText) view.findViewById(R.id.start_date);
        final EditText endDate = (EditText) view.findViewById(R.id.end_date);
        Button submit = (Button) view.findViewById(R.id.submit);
        setdatepicker(startDate);
        setdatepicker(endDate);
        employee_input.setThreshold(1);
        employee_input.setAdapter(adapterAutoCompleteTextView);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<AttendanceItem> attendanceItemsList1 = new ArrayList<>();

                try {


                    String d1 = startDate.getText().toString();
                    String d2 = endDate.getText().toString();
                    String employee_name = employee_input.getText().toString();
                    Date date1 = null, date2 = null;
                    if (!d1.isEmpty()) {
                        date1 = new SimpleDateFormat("dd-MM-yy").parse(d1);
                        date1 = new Date(date1.getTime() - 2);
                    }
                    if (!d2.isEmpty()) {
                        date2 = new SimpleDateFormat("dd-MM-yy").parse(d2);
                        date2 = new Date(date2.getTime() + 2);
                    }
                    for (int i = 0; i < attendanceItemsList.size(); i++) {
                        Date d = new SimpleDateFormat("dd-MM-yy").parse(attendanceItemsList.get(i).getDate());

                        if (employee_name.isEmpty() && d1.isEmpty() && d2.isEmpty()) {
                            attendanceItemsList1.add(attendanceItemsList.get(i));
                        } else if (employee_name.isEmpty() && d1.isEmpty()) {
                            if (d.before(date2))
                                attendanceItemsList1.add(attendanceItemsList.get(i));
                        } else if (employee_name.isEmpty() && d2.isEmpty()) {
                            if (d.after(date1))
                                attendanceItemsList1.add(attendanceItemsList.get(i));
                        } else if (d1.isEmpty() && d2.isEmpty()) {
                            if (employee_name.equals(attendanceItemsList.get(i).getName()))
                                attendanceItemsList1.add(attendanceItemsList.get(i));
                        } else if (employee_name.isEmpty()) {
                            if (d.after(date1) && d.before(date2))
                                attendanceItemsList1.add(attendanceItemsList.get(i));
                        } else if (d1.isEmpty()) {
                            if (employee_name.equals(attendanceItemsList.get(i).getName()) && d.before(date2))
                                attendanceItemsList1.add(attendanceItemsList.get(i));
                        } else if (d2.isEmpty()) {
                            if (employee_name.equals(attendanceItemsList.get(i).getName()) && d.after(date1))
                                attendanceItemsList1.add(attendanceItemsList.get(i));
                        } else {
                            if (employee_name.equals(attendanceItemsList.get(i).getName()) && d.after(date1) && d.before(date2))
                                attendanceItemsList1.add(attendanceItemsList.get(i));
                        }


                    }

                    AttendanceAdapter attendanceAdapter = new AttendanceAdapter(attendanceItemsList1);
                    listView.setAdapter(attendanceAdapter);

                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        });


        return view;
    }


    //            for setdate picker
    void setdatepicker(final EditText edittext) {
        final Calendar myCalendar = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "dd-MM-yy"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                edittext.setText(sdf.format(myCalendar.getTime()));
            }

        };

        edittext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(getActivity(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


    }


}
