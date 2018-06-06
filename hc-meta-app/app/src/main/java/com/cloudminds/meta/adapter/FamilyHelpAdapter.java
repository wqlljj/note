package com.cloudminds.meta.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cloudminds.meta.R;

/**
 * Created by tiger on 17-4-11.
 */

public class FamilyHelpAdapter extends BaseAdapter {

    private Context mContext;
    private String [] items;

    public FamilyHelpAdapter(Context context,String [] items){
        this.mContext = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if(view == null){
            view = LayoutInflater.from(mContext).inflate(R.layout.family_help_item,null);
            holder = new ViewHolder();
            holder.title = (TextView) view.findViewById(R.id.item_title);
            view.setTag(holder);
        }else {
            holder = (ViewHolder) view.getTag();
        }
        if(i < items.length)
            holder.title.setText(items[i]);
        return view;
    }

    public final class ViewHolder {
        public TextView title;
    }
}


