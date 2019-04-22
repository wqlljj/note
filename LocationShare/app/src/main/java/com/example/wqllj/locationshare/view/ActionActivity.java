package com.example.wqllj.locationshare.view;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wqllj.locationshare.R;
import com.example.wqllj.locationshare.databinding.ActivityActionBinding;
import com.example.wqllj.locationshare.db.DbManager;
import com.example.wqllj.locationshare.db.bean.EventBean;
import com.example.wqllj.locationshare.db.operator.EventOperator;
import com.example.wqllj.locationshare.view.adapter.ActionAdapter;

import java.util.List;

public class ActionActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityActionBinding dataBinding;
    private ActionAdapter actionAdapter;
    private Long personId;
    public static String KEY_PERSONID = "KEY_PERSONID";
    private String TAG = "ActionActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action);
        personId=getIntent().getLongExtra(KEY_PERSONID,0l);
        Log.e(TAG, "onCreate: personId = "+personId );
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_action);
        initActionBar();
        List<EventBean> eventBeen = DbManager.getInstance().getOperator(EventOperator.class).queryByPersonId(personId);
        Log.e(TAG, "onCreate: eventBeen.size = "+eventBeen.size() );
        actionAdapter = new ActionAdapter(this,eventBeen);
        dataBinding.actionView.setAdapter(actionAdapter);
        dataBinding.actionView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.set(10,5,10,5);
            }
        });
    }

    private void initActionBar() {
        dataBinding.actionBar.setEntity(new ActionBarEntity("历史活动") {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.back:
                        finish();
                        break;
                    case R.id.menu:
                        Toast.makeText(view.getContext(), "开发中", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(this, "点击", Toast.LENGTH_SHORT).show();
        switch (v.getId()){
            case R.id.back:
                finish();
                break;
            case R.id.menu:
                Toast.makeText(this, "开发中", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
