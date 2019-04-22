package com.example.wqllj.locationshare.view.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.text.TextUtilsCompat;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.wqllj.locationshare.R;
import com.example.wqllj.locationshare.db.bean.EventBean;
import com.example.wqllj.locationshare.util.DateUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cloud on 2018/9/17.
 */

public class ActionAdapter extends RecyclerView.Adapter<ActionAdapter.ViewHolder> {
    private final Context context;
    List<EventBean> items;
    private String TAG = "ActionAdapter";

    public ActionAdapter(Context context, List<EventBean> items) {
        this.context = context;
        this.items = items;
    }

    public ActionAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.item_action;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(viewType, null);
        ViewHolder viewHolder = new ViewHolder(inflate);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EventBean eventBean = items.get(position);
        holder.name.setText(eventBean.getName());
        holder.date.setText(DateUtil.dateToString(eventBean.getPointBean().getDate()));
        holder.expandView.removeAllViews();
        if(!TextUtils.isEmpty(eventBean.getData())){
            Log.e(TAG, "onBindViewHolder: data = "+ eventBean.getData());
            TextView textView = new TextView(holder.expandView.getContext());
            textView.setText(eventBean.getData());
            LinearLayoutCompat.LayoutParams params = new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            textView.setLayoutParams(params);
            textView.setTextSize(20);
            holder.expandView.addView(textView);
        }
    }

    @Override
    public int getItemCount() {
        return items==null?0:items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private final ImageView headImage;
        private final TextView name;
        private final TextView date;
        private final LinearLayout expandView;

        public ViewHolder(View itemView) {
            super(itemView);
            headImage = ((ImageView) itemView.findViewById(R.id.image_head));
            name = ((TextView) itemView.findViewById(R.id.name));
            date = ((TextView) itemView.findViewById(R.id.date));
            expandView = ((LinearLayout) itemView.findViewById(R.id.expandView));
        }
    }
}
