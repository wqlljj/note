package com.cloudminds.meta.accesscontroltv.view.adapter;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cloudminds.meta.accesscontroltv.constant.Constant;
import com.cloudminds.meta.accesscontroltv.bean.PersonInfoBean;
import com.cloudminds.meta.accesscontroltv.R;
import com.cloudminds.meta.accesscontroltv.util.Utils;
import com.github.onlynight.multithreaddownloader.library.DownloadManager;
import com.github.onlynight.multithreaddownloader.library.FileDownloader;
//import com.squareup.picasso.Picasso;
//import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Wasabeef on 2015/01/03.
 */
public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {

  private Context mContext;
  private List<PersonInfoBean> mDataSet;
  private HashMap<Integer,Boolean> mShowSet;
  private int height;
  private String TAG = "MainAdapter";

  public MainAdapter(Context context, List<PersonInfoBean> dataSet) {
    mContext = context;
    mDataSet = dataSet;
    mShowSet=new HashMap<>();
    for (int i = 0; i < dataSet.size(); i++) {
      mShowSet.put(i,false);
    }
  }
  protected Animator[] getAddAnimators(View view) {
    ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.5f, 1f);
    ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.5f, 1f);
    return new ObjectAnimator[] { scaleX, scaleY };
  }
//  protected Animator[] getRemoveAnimators(View view) {
//    ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.5f);
//    ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.5f);
//    return new ObjectAnimator[] { scaleX, scaleY };
//  }
  private void startAnim(View view, float scale){
    Log.e(TAG, "startAnim: a "+scale+"   "+view );
    scale=scale>1?1:scale<0?0:scale;
    ViewCompat.setScaleX(view,scale);
    ViewCompat.setScaleY(view,scale);
  }
  private void outAnim(){

  }
  @Override
  public void onAttachedToRecyclerView(RecyclerView recyclerView) {
    super.onAttachedToRecyclerView(recyclerView);
    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      public boolean isInit=false;

      @Override
      public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        Log.e(TAG, "onScrollStateChanged: "+newState );
        }
      private void init(RecyclerView recyclerView,int frist,int last){
        isInit=true;
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        //判断是当前layoutManager是否为LinearLayoutManager
        // 只有LinearLayoutManager才有查找第一个和最后一个可见view位置的方法
        if (layoutManager instanceof LinearLayoutManager) {
          LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
          for (int i = frist; i <= last; i++) {
            Log.e(TAG, "init: " +i);
            mShowSet.put(i,true);
            View view = linearManager.getChildAt(i-frist);
            if(i!=last||1.0* Math.abs(recyclerView.getHeight()-view.getTop())/view.getHeight()>0.55) {
              for (Animator anim : getAddAnimators(view)) {
                anim.setDuration(1000).start();
                anim.setInterpolator(new OvershootInterpolator(0.5f));
              }
            }
          }
        }

      }
      long lastScrollTime=0;
      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if(System.currentTimeMillis()-lastScrollTime<100)return;
        lastScrollTime=System.currentTimeMillis();
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        //判断是当前layoutManager是否为LinearLayoutManager
        // 只有LinearLayoutManager才有查找第一个和最后一个可见view位置的方法
        if (layoutManager instanceof LinearLayoutManager) {
          LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
          //获取最后一个可见view的位置
          int lastItemPosition = linearManager.findLastVisibleItemPosition();
          //获取第一个可见view的位置
          int firstItemPosition = linearManager.findFirstVisibleItemPosition();
          if(!isInit) {
            init(recyclerView,firstItemPosition, lastItemPosition);
          }
          Log.e(TAG, "onScrolled: "+firstItemPosition+"  "+lastItemPosition+"  "+dx+"   "+dy );
          View fristView = linearManager.getChildAt(0);
          if(fristView==null)return;
          Log.e(TAG, "startAnim: fristView" );
          startAnim(fristView,1.0f*fristView.getBottom()/fristView.getHeight()/2+0.5f);

          for (int i = 1; i < lastItemPosition-firstItemPosition; i++) {
            Log.e(TAG, "startAnim: fristView +"+i );
            startAnim(linearManager.getChildAt(i),1);
          }
          Log.e(TAG, "startAnim: endView" );
          View endView = linearManager.getChildAt(lastItemPosition-firstItemPosition);
          startAnim(endView,1.0f*(recyclerView.getHeight()-endView.getTop())/endView.getHeight()*0.5f+0.5f);
          System.out.println(lastItemPosition + "   " + fristView.getTop() + "   " + firstItemPosition);

        }
      }
    });
  }

  int i=0;
  @Override
  public void onViewAttachedToWindow(ViewHolder holder) {
    super.onViewAttachedToWindow(holder);
        Log.e(TAG, "onViewAttachedToWindow: "+((RecyclerView) holder.itemView.getParent()).getHeight()+"   "+holder.itemView.getTop()+"  "+holder.itemView.getHeight());
    i=i>100?1:i;
    mHandler.sendMessage(Message.obtain(mHandler,i++,holder.itemView));


  }
  Handler mHandler=new Handler() {
    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      if(((View)msg.obj).getTop()!=0&&((View) msg.obj).getParent()!=null) {
//        Toast.makeText(mContext,  ((RecyclerView) ((View) msg.obj).getParent()).getHeight()+"  "+((View) msg.obj).getHeight(), Toast.LENGTH_SHORT).show();
        startAnim((View) msg.obj, 1.0f * (((RecyclerView) ((View) msg.obj).getParent()).getHeight() - ((View) msg.obj).getTop()) / ((View) msg.obj).getHeight() * 0.5f + 0.5f);
      }else{
        mHandler.sendMessageDelayed(Message.obtain(mHandler,msg.what,msg.obj),100);
      }
    }
  };

  @Override
  public int getItemViewType(int position) {
    return R.layout.item_personinfo;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(mContext).inflate(viewType, parent, false);
    return new ViewHolder(v,viewType);
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    Log.e(TAG, "onBindViewHolder: "+R.layout.item_top+"  "+R.layout.item_personinfo );
    Log.e(TAG, "onBindViewHolder: "+position+"  "+holder.viewType+"   "+holder.itemView );
    switch (holder.viewType){
      case R.layout.item_personinfo:
        PersonInfoBean personInfoBean = mDataSet.get(position);
        if(personInfoBean.isAvailable()) {
          holder.itemView.setVisibility(View.VISIBLE);
          Glide.with(holder.person_head.getContext())
                  .load(Constant.getImagebaseUrl() +personInfoBean.getPhotoPath())
                  .diskCacheStrategy( DiskCacheStrategy.RESULT )
                  .into(holder.person_head);
//          Uri uri = Uri.parse(Constant.getImagebaseUrl() +personInfoBean.getPhotoPath());
//          holder.person_head.setImageURI(uri);
//          Picasso.with(holder.person_head.getContext()).load(Constant.getImagebaseUrl() +personInfoBean.getPhotoPath()).into(holder.person_head);

          holder.name_en.setText(personInfoBean.getEname());
          holder.name_zh.setText(personInfoBean.getName());
          holder.job.setText(personInfoBean.getPosition());
          int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
          if(!TextUtils.isEmpty(personInfoBean.getWelcomeMsg())){
            holder.welcome_msg.setVisibility(View.VISIBLE);
            holder.welcome_msg.setText(personInfoBean.getWelcomeMsg());
          }else {
            holder.welcome_msg.setVisibility(View.GONE);
//            if (hour >= 6 && hour < 12) {
//              holder.welcome_msg.setText("上午好");
//            } else if (hour >= 12 && hour < 19) {
//              holder.welcome_msg.setText("下午好");
//            } else {
//              holder.welcome_msg.setText("晚上好");
//            }
          }
        }else{
          holder.itemView.setVisibility(View.INVISIBLE);
        }
        break;
      case R.layout.item_top:
        break;
    }

  }

  @Override
  public int getItemCount() {
    return mDataSet.size();
  }

  public void remove(int position) {
    PersonInfoBean remove = mDataSet.remove(position);
    Log.e(TAG, "remove: "+getItemCount()+"  "+remove );
    notifyItemRemoved(position);
  }
  public void add(PersonInfoBean bean) {
    mDataSet.add(bean);
    Log.e(TAG, "add: "+getItemCount()+"  "+bean );
    notifyItemInserted(mDataSet.size()-1);
  }
  public void add(PersonInfoBean bean, int position) {
    Log.e(TAG, "add: "+position+"   "+bean );
    mDataSet.add(position, bean);
    notifyItemInserted(position);
  }

  public void clear() {
    Log.e(TAG, "clear: " );
    mDataSet.clear();
    notifyDataSetChanged();
  }

  static class ViewHolder extends RecyclerView.ViewHolder {

    private  View person_info;
    public int viewType;
    public   ImageView birthday_icon;
    private  ImageView person_bg_head;
    public  TextView name_zh;
    public  TextView job;
    public  TextView welcome_msg;
    public ImageView person_head;
    public TextView name_en;

    public ViewHolder(View itemView,int viewType) {
      super(itemView);
      this.viewType = viewType;
      switch (viewType){
        case R.layout.item_personinfo:
          person_info=itemView.findViewById(R.id.person_info);
          person_head = (ImageView) itemView.findViewById(R.id.person_head);
          birthday_icon = (ImageView) itemView.findViewById(R.id.birthday_icon);
          person_bg_head = (ImageView) itemView.findViewById(R.id.person_bg_head);
          name_en = (TextView) itemView.findViewById(R.id.name_en);
          name_zh = (TextView) itemView.findViewById(R.id.name_zh);
          job = (TextView) itemView.findViewById(R.id.job);
          welcome_msg = (TextView) itemView.findViewById(R.id.welcome_msg);
          Utils.setFontType(this.name_en, "Roboto-Medium.ttf");
          Utils.setFontType(this.name_zh, "Roboto-Medium.ttf");
          Utils.setFontType(this.job, "Roboto-Regular.ttf");
          Utils.setFontType(this.welcome_msg, "Roboto-Medium.ttf");
          break;
        case R.layout.item_top:
          break;
      }
    }


  }
}
