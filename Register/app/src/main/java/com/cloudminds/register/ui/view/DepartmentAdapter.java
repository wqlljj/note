package com.cloudminds.register.ui.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.cloudminds.register.repository.network.bean.Department;

import java.util.ArrayList;
import java.util.List;

/**
 * Created
 */

public class DepartmentAdapter extends ArrayAdapter<Department.DataBean> {

    private List<Department.DataBean> mDepartment;

    public DepartmentAdapter(@NonNull Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public void setList(List<Department.DataBean> items) {
        this.mDepartment = new ArrayList<>(items);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDepartment == null ? super.getCount() : mDepartment.size();
    }

    @Nullable
    @Override
    public Department.DataBean getItem(int position) {
        return mDepartment == null ? super.getItem(position) : mDepartment.get(position);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        View view = convertView;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.departmentName = view.findViewById(android.R.id.text1);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.departmentName.setText(mDepartment.get(position).getName());
        return view;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        View view = convertView;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.departmentName = view.findViewById(android.R.id.text1);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.departmentName.setText(mDepartment.get(position).getName());
        return view;
    }

    static class ViewHolder {
        TextView departmentName;
    }
}
