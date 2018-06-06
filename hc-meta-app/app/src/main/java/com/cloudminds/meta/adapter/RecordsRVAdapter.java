package com.cloudminds.meta.adapter;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.cloudminds.meta.R;
import com.cloudminds.meta.bean.ChatMessage;

import java.util.ArrayList;

/**
 * Created by SX on 2017/2/23.
 */

public class RecordsRVAdapter extends BaseRVAdapter<ChatMessage>{
    ArrayList<ChatMessage> list;

    public RecordsRVAdapter(Context context, @NonNull ArrayList<ChatMessage> list, @IdRes int[] layouts) {
        super(context, list, layouts);
        this.list=list;
    }

    @Override
    public int getItemLayoutType(ChatMessage chatMessage) {
        if(chatMessage.getType()==ChatMessage.Type.CHAT_LEFT){
            return 0;
        }else if(chatMessage.getType()==ChatMessage.Type.CHAT_RGIHT){
            return 1;
        }
        return -1;
    }
    @Override
    public BaseRVAdapter<ChatMessage>.ViewHolder createVH(View view,int index) {
        return new ViewHolder(view,index);
    }
//    @Override
    public void fillData(BaseRVAdapter<ChatMessage>.ViewHolder holder, ChatMessage chatMessage) {
        ViewHolder vh = (ViewHolder) holder;
        switch (vh.index) {
            case 0:
            case 1:
            String content = "";
            if (chatMessage.getContentType() == ChatMessage.ContentType.CONTENT_STRING) {
                content = ((String) chatMessage.getData());
            }
            vh.chat_content.setText(content);
            vh.chat_time.setText(chatMessage.getTime());
                break;
        }
    }

    @Override
    public void onBindViewHolder(BaseRVAdapter.ViewHolder holder, int position) {
        fillData(holder,list.get(position));
    }


    public class ViewHolder extends BaseRVAdapter.ViewHolder{

        private final int index;
        public   TextView chat_time;
        public   TextView chat_content;
        //index:布局索引
        public ViewHolder(View itemView, int index) {
            super(itemView);
            this.index = index;
            switch (index){
                case 0:
                case 1:
                    chat_content = ((TextView) itemView.findViewById(R.id.chat_tv_content));
                    chat_time = ((TextView) itemView.findViewById(R.id.chat_tv_date));
                    break;
            }
        }
    }
}
