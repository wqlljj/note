package com.cloudminds.register.ui.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.cloudminds.register.R;
import com.cloudminds.register.repository.db.entity.EmployeeEntity;
import com.cloudminds.register.utils.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created
 */
public class NameFilterAdapter extends ArrayAdapter<EmployeeEntity> {

    private static final String TAG = "NameFilterAdapter";
    private final Context context;
    private List<EmployeeEntity> items, tempItems, suggestions;

    public NameFilterAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
        this.context = context;
    }

    public void setList(List<EmployeeEntity> items) {
        this.items = items;
        tempItems = new ArrayList<>(); // this makes the difference.
        suggestions = new ArrayList<>();
        Log.d(TAG, "tempItems = " + tempItems);
        Log.d(TAG, "suggestions = " + suggestions);
        Log.d(TAG, "items = " + items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        View view = convertView;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.autocomplete_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.autocompleteItem = view.findViewById(R.id.lbl_name);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Log.d(TAG, "getView employee = " + items.get(position));
        if (getItem(position) != null) {
            viewHolder.autocompleteItem.setText(String.format(getContext().getString(R.string.name_filter_display), getItem(position).getName(), getItem(position).getEname()));
        }
        return view;
    }

    static class ViewHolder {
        TextView autocompleteItem;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return nameFilter;
    }
    long freshTime = 0L;
     Handler mHandler = new Handler()
    {
        public void handleMessage(Message message)
        {
            switch (message.what){
                case 100:
                    if(mHandler.hasMessages(102)){
                        mHandler.removeMessages(102);
                    }
                    if(System.currentTimeMillis()-freshTime<1000){
                        Log.e("NameFilterAdapter", "handleMessage: return" + (System.currentTimeMillis() - freshTime));
                        Message msg = new Message();
                        msg.what = 102;
                        msg.obj = message.obj;
                        mHandler.sendMessageDelayed(msg, 1000L - (System.currentTimeMillis() - freshTime));
                        return;
                    }
                    freshTime=System.currentTimeMillis();
                    NameFilterAdapter.this.clear();
                    NameFilterAdapter.this.addAll((Collection)message.obj);
                    NameFilterAdapter.this.notifyDataSetChanged();
                    break;
                case 102:
                    freshTime=System.currentTimeMillis();
                    NameFilterAdapter.this.clear();
                    NameFilterAdapter.this.addAll((Collection)message.obj);
                    NameFilterAdapter.this.notifyDataSetChanged();
                    break;
            }
        }
    };
    /**
     * Custom Filter implementation for custom suggestions we provide.
     */
    private final Filter nameFilter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((EmployeeEntity) resultValue).getName();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null && tempItems != null) {
                tempItems.clear();
                for (EmployeeEntity employee : items) {
                    if (isAddSuggestions(employee, constraint)) {
                        tempItems.add(employee);
                        Log.d("EmployeeFragment", "suggestions entity = " + employee);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = tempItems;
                filterResults.count = tempItems.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
                    if(results.count>0) {
                        Message message = new Message();
                        message.obj = results.values;
                        message.what = 100;
                        mHandler.sendMessageDelayed(message, 1000L);
                    }
//            List<EmployeeEntity> filterList = (List<EmployeeEntity>) results.values;
//            if (results.count > 0) {
//                clear();
//                for (EmployeeEntity people : filterList) {
//                    add(people);
//                    notifyDataSetChanged();
//                }
//            }
        }
    };

    private boolean isAddSuggestions(EmployeeEntity employee, CharSequence constraint) {
        String name = employee.getName();
        String eName = employee.getEname();

        Log.d(TAG, "name:" + name);
        Log.d(TAG, "eName:" + eName);
        Log.d(TAG, "constraint:" + constraint);
        return ((!TextUtils.isEmpty(name)) && (name.contains(constraint))) || ((!TextUtils.isEmpty(eName)) && (eName.toLowerCase().contains(constraint.toString().toLowerCase())));
    }
}
