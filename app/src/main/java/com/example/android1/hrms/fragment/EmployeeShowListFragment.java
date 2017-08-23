package com.example.android1.hrms.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;

import com.example.android1.hrms.R;
import com.example.android1.hrms.RegistrationEmployeeActivity;
import com.example.android1.hrms.adapter.EmployeeAdapter;
import com.example.android1.hrms.database.EmployeeDatabaseHelper;
import com.example.android1.hrms.modelclass.EmployeeDataModelClass;

import java.util.ArrayList;


public class EmployeeShowListFragment extends Fragment {

    ListView listView;
    String TAG = "EmployeeShowListFragment";
    EmployeeDatabaseHelper employeeDatabaseHelper;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_employee_show_list, container, false);


        try {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        } catch (Exception e) {
            e.printStackTrace();
        }


        employeeDatabaseHelper = new EmployeeDatabaseHelper(getActivity());
        listView = (ListView) view.findViewById(R.id.listview);


        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fabemployee);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), RegistrationEmployeeActivity.class);
                startActivity(intent);
            }
        });


        Cursor c = employeeDatabaseHelper.getAllEmployee();
        final ArrayList<EmployeeDataModelClass> employeeList = new ArrayList<EmployeeDataModelClass>();
        if (c != null) {
            while (c.moveToNext()) {
                employeeList.add(new EmployeeDataModelClass(c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getBlob(5), c.getBlob(6)));
                Log.d(TAG, c.getString(1));
                Log.d(TAG, c.getString(2));
                Log.d(TAG, c.getString(3));
                Log.d(TAG, c.getString(4));

            }
        }

        EmployeeAdapter employeeAdapter = new EmployeeAdapter(employeeList);
        listView.setAdapter(employeeAdapter);

        employeeAdapter.notifyDataSetChanged();

        return view;
    }


}
