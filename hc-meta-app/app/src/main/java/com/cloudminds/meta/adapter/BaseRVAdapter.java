package com.cloudminds.meta.adapter;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.util.ArrayList;
import java.util.List;


/**
 * Created by SX on 2017/2/24.
 */

public abstract class BaseRVAdapter<D> extends RecyclerView.Adapter<BaseRVAdapter.ViewHolder> {
    private final Context context;
    private final int[] layouts;
    private final LayoutInflater inflater;
    String TAG="BaseRVAdapter";
    ArrayList<D> list=new ArrayList();

    public BaseRVAdapter(Context context, @NonNull ArrayList<D> list , @IdRes int[] layouts) {
        this.context = context;
        this.list = list;
        this.layouts = layouts;
        inflater = LayoutInflater.from(context);
    }
    public void addData(D d){
        list.add(d);
        notifyItemInserted(list.size()-1);
    }
    public void removeData(D d){
        list.remove(d);
        notifyDataSetChanged();
    }
    public void addDatas(List<D> datas){
        list.addAll(datas);
        notifyItemRangeInserted(list.size()-datas.size(),datas.size());
    }
    public void removedatas(List<D> datas){
        list.removeAll(datas);
        notifyDataSetChanged();
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType<0||viewType>layouts.length){
            Log.e(TAG,"viewType=="+viewType);
            return null;
        }
        return createVH(inflater.inflate(layouts[viewType],parent,false),viewType);
    }

    @Override
    public int getItemViewType(int position) {
        return getItemLayoutType(list.get(position));
    }

//    @Override
//    public void onBindViewHolder(ViewHolder holder, int position) {
//        fillData(holder,list.get(position));
//    }
    //    @Override
//    public void onBindViewHolder(ViewHolder holder, int position) {
////        fillData(holder,list.get(position));
//    }
    public abstract int getItemLayoutType(D d);
    public abstract ViewHolder createVH(View view,int index);
//    public abstract void fillData(ViewHolder holder,D d);
    @Override
    public int getItemCount() {
        return list.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
