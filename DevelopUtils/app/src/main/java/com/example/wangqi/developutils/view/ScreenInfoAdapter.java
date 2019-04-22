package com.example.wangqi.developutils.view;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.example.wangqi.developutils.R;
import com.example.wangqi.developutils.application.Constant;
import com.example.wangqi.developutils.bean.ScreenBean;
import com.example.wangqi.developutils.util.ScreenUtil;
import com.example.wangqi.developutils.util.SharePreferenceUtils;
import com.example.wangqi.developutils.util.ToastOrLogUtil;

import java.util.ArrayList;

/**
 * Created by cloud on 2018/8/6.
 */

public class ScreenInfoAdapter extends RecyclerView.Adapter<ScreenInfoAdapter.ViewHolder> {
    ArrayList<ScreenBean> screenBeen = new ArrayList<>();
    private View.OnLongClickListener longClickListener;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new ViewHolder(view, viewType);
    }

    public void addDatas(ArrayList<ScreenBean> screenBeen) {
        this.screenBeen.addAll(screenBeen);
        notifyDataSetChanged();
    }

    public void addData(ScreenBean screenBean) {
        this.screenBeen.add(screenBean);
        notifyItemChanged(screenBeen.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        return position == 1 ? R.layout.layout_screenadd_item : R.layout.layout_screeninfo_item;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position != 1) {
            ScreenBean screenBean = screenBeen.get(position);
            holder.bean = screenBean;
            if (screenBean.getWidth_px() == 0) {
                holder.editInfo_cb.setChecked(true);
            } else {
                holder.screen_width_px.setText("" + screenBean.getWidth_px());
                holder.screen_height_px.setText("" + screenBean.getHeight_px());
                holder.density.setText("" + screenBean.getDensity());
                holder.scaleDensity.setText("" + screenBean.getScaledDensity());
                holder.screenInfo.setText(screenBean.toSimpleString());
            }
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return screenBeen.size();
    }

    public ArrayList<ScreenBean> getData() {
        return screenBeen;
    }

    public void setLongClickListener(View.OnLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public View.OnLongClickListener getLongClickListener() {
        return longClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

        public final int type;
        private TextView screenInfo;
        private CheckBox editInfo_cb;
        public EditText scaleDensity;
        public EditText density;
        public EditText screen_width_px;
        public EditText screen_height_px;
        public ScreenBean bean;

        public ViewHolder(View itemView, int type) {
            super(itemView);
            this.type = type;
            switch (type) {
                case R.layout.layout_screenadd_item:
                    itemView.findViewById(R.id.tv_addScreenInfo).setOnClickListener(this);
                    break;
                case R.layout.layout_screeninfo_item:
                    if(longClickListener!=null){
                        itemView.setOnLongClickListener(longClickListener);
                    }
                    editInfo_cb = ((CheckBox) itemView.findViewById(R.id.editInfo));
                    editInfo_cb.setOnCheckedChangeListener(this);
                    screen_width_px = ((EditText) itemView.findViewById(R.id.screen_width_px));
                    screen_height_px = ((EditText) itemView.findViewById(R.id.screen_height_px));
                    density = ((EditText) itemView.findViewById(R.id.density));
                    scaleDensity = ((EditText) itemView.findViewById(R.id.scaleDensity));
                    screenInfo = ((TextView) itemView.findViewById(R.id.screenInfo));
                    break;
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            ToastOrLogUtil.e("onCheckedChanged: " + isChecked);
            if (!isChecked) {
                try {
                    Integer width_px = Integer.valueOf(screen_width_px.getText().toString());
                    Integer height_px = Integer.valueOf(screen_height_px.getText().toString());
                    Float density = Float.valueOf(this.density.getText().toString());
                    Float scaledDensity = Float.valueOf(scaleDensity.getText().toString());
                    bean.setData(width_px,
                            height_px,
                            density,
                            scaledDensity);
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastOrLogUtil.show(screen_height_px.getContext(), "数据有误，保存失败");
                    editInfo_cb.setChecked(true);
                    return;
                }
                notifyItemChanged(screenBeen.indexOf(bean));
                ToastOrLogUtil.show(screen_height_px.getContext(), "保存成功");
                ScreenUtil.executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        ToastOrLogUtil.e("run: " + screenBeen);
                        SharePreferenceUtils.setPrefString(Constant.KEY_SCREENINFO, "" + screenBeen);
                    }
                });
            }
            scaleDensity.setEnabled(isChecked);
            density.setEnabled(isChecked);
            screen_width_px.setEnabled(isChecked);
            screen_height_px.setEnabled(isChecked);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_addScreenInfo:
                    ToastOrLogUtil.e("onClick: tv_addScreenInfo");
                    screenBeen.add(2, new ScreenBean(0, 0, 0, 0));
                    notifyDataSetChanged();
                    break;
            }
        }
    }

}
