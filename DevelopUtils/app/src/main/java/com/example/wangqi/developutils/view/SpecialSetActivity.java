package com.example.wangqi.developutils.view;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wangqi.developutils.R;
import com.example.wangqi.developutils.bean.DimenItemBean;
import com.example.wangqi.developutils.bean.ScreenBean;
import com.example.wangqi.developutils.databinding.ActivitySpecialSetBinding;
import com.example.wangqi.developutils.util.ScreenUtil;
import com.example.wangqi.developutils.util.ToastOrLogUtil;
import com.leon.lfilepickerlibrary.LFilePicker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.example.wangqi.developutils.application.Constant.baseDimensPath;

public class SpecialSetActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener, RadioGroup.OnCheckedChangeListener, AdapterView.OnItemLongClickListener {
    private String TAG = "SpecialSetActivity";

    private ActivitySpecialSetBinding dataBinding;
    private ScreenBean screenBean;
    private static final int REQUESTCODE_DIMENS_X = 1002;
    private static final int REQUESTCODE_DIMENS_Y = 1003;
    private ArrayAdapter<String> adpater;
//    private ArrayList<DimenItemBean> itemBeen = new ArrayList<>();
    private ArrayList<DimenItemBean> xItemBeen;
    private ArrayList<DimenItemBean> yItemBeen;
    private ArrayAdapter<String> serachAdpater;
    DimenItemBean selectBean;
    HashSet<DimenItemBean> settingBeen =new HashSet<>();
    HashSet<DimenItemBean> setXBeen =new HashSet<>();
    HashSet<DimenItemBean> setYBeen =new HashSet<>();

    String oprate;
    String num;
    private ArrayAdapter<String> settedAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_special_set);
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_special_set);
        screenBean = (ScreenBean) getIntent().getSerializableExtra("ScreenBean");
        if (screenBean != null) {
            initView();
        } else {
            Toast.makeText(this, "无屏幕信息！", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    OnClickListener selectXFile = new OnClickListener() {
        @Override
        public void onClick() {
            selectFile(REQUESTCODE_DIMENS_X);
        }
    };
    OnClickListener selectYFile = new OnClickListener() {
        @Override
        public void onClick() {
            selectFile(REQUESTCODE_DIMENS_Y);
        }
    };

    private void selectFile(int requestCode) {
        new LFilePicker()
                .withActivity(this)
                .withRequestCode(requestCode)
                .withStartPath(baseDimensPath)
                .withTitle("xml文件选择")
                .withChooseMode(true)
                .withMaxNum(1)
                .withFileFilter(new String[]{".xml"})
                .start();
    }

    private void initView() {
        dataBinding.setIsSettingX(true);
        dataBinding.title.setText(screenBean.getWidth_px() + "*" + screenBean.getHeight_px());
        dataBinding.setSelectXFile(selectXFile);
        dataBinding.setSelectYFile(selectYFile);
        settedAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        adpater = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        serachAdpater = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        dataBinding.listView.setAdapter(adpater);
        dataBinding.listView.setOnItemClickListener(this);
        dataBinding.setted.setAdapter(settedAdapter);
        dataBinding.setted.setOnItemLongClickListener(this);
        dataBinding.search.setAdapter(serachAdpater);
        dataBinding.search.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //这里设置文本值，否则默认显示赋值的文本是Cursor的toString()
                TextView textView = (TextView) view;
                dataBinding.search.setText(textView.getText());
                ArrayList<DimenItemBean> itemBeen = dataBinding.radio.getCheckedRadioButtonId()==R.id.x?xItemBeen:yItemBeen;
                for (DimenItemBean dimenItemBean : itemBeen) {
                    if (dimenItemBean.getName().equals(textView.getText().toString().split(" ")[0])) {
                        selectBean = dimenItemBean;
                        dataBinding.setSetEnable(true);
                        Toast.makeText(SpecialSetActivity.this, "" + selectBean.getName(), Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        });
        dataBinding.search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (dataBinding.add.isEnabled() && (selectBean == null || !s.toString().equals(selectBean.getName()))) {
                    dataBinding.setSetEnable(false);
                }

            }
        });
        dataBinding.save.setOnClickListener(this);
        dataBinding.add.addTextChangedListener(new TextWatcher() {
            boolean isReset=false;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!dataBinding.add.hasFocus()){
                    Log.e(TAG, "afterTextChanged: return" );
                    return;
                }
                if(!s.equals("0")) {
                    oprate = DimenItemBean.TYPE_OPRATION_ADD;
                    num = s.toString();
                }else{
                    oprate="";
                    num="";
                }
                dataBinding.divide.setText("0");
                dataBinding.minus.setText("0");
                dataBinding.multiply.setText("0");
            }
        });
        dataBinding.minus.addTextChangedListener(new TextWatcher() {
            boolean isReset=false;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!dataBinding.minus.hasFocus()){
                    return;
                }
                if(!s.equals("0")) {
                    oprate = DimenItemBean.TYPE_OPRATION_MINUS;
                    num = s.toString();
                }else{
                    oprate="";
                    num="";
                }
                dataBinding.add.setText("0");
                dataBinding.divide.setText("0");
                dataBinding.multiply.setText("0");
            }
        });
        dataBinding.multiply.addTextChangedListener(new TextWatcher() {
            boolean isReset=false;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!dataBinding.multiply.hasFocus()){
                    return;
                }
                if(!s.equals("0")) {
                    oprate = DimenItemBean.TYPE_OPRATION_MULTIPLY;
                    num = s.toString();
                }else{
                    oprate="";
                    num="";
                }
                dataBinding.add.setText("0");
                dataBinding.minus.setText("0");
                dataBinding.divide.setText("0");
            }
        });
        dataBinding.divide.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!dataBinding.divide.hasFocus()){
                    return;
                }
                if(!s.equals("0")) {
                    oprate = DimenItemBean.TYPE_OPRATION_DIVIDE;
                    num = s.toString();
                }else{
                    oprate="";
                    num="";
                }
                dataBinding.add.setText("0");
                dataBinding.minus.setText("0");
                dataBinding.multiply.setText("0");
            }
        });
        dataBinding.xsp.setOnClickListener(this);
        dataBinding.xdp.setOnClickListener(this);
        dataBinding.xpx.setOnClickListener(this);
        dataBinding.ydp.setOnClickListener(this);
        dataBinding.ypx.setOnClickListener(this);
        dataBinding.ysp.setOnClickListener(this);
        dataBinding.back.setOnClickListener(this);
        dataBinding.run.setOnClickListener(this);
        dataBinding.radio.setOnCheckedChangeListener(this);
        dataBinding.showSet.setOnClickListener(this);
        dataBinding.dismissShowSet.setOnClickListener(this);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUESTCODE_DIMENS_X:
                    if (data.hasExtra("paths")) {
                        //If it is a file selection mode, you need to get the path collection of all the files selected
                        //List<String> list = data.getStringArrayListExtra(Constant.RESULT_INFO);//Constant.RESULT_INFO == "paths"
                        List<String> list = data.getStringArrayListExtra("paths");
                        ToastOrLogUtil.e(TAG, list.toString());
                        dataBinding.setXPath(list.get(0));
                        readInfo(list.get(0), DimenItemBean.TYPE_X, setXBeen);
                    } else {
                        //If it is a folder selection mode, you need to get the folder path of your choice
                        String path = data.getStringExtra("path");
                        ToastOrLogUtil.e(TAG, path);
                    }
                    break;
                case REQUESTCODE_DIMENS_Y:
                    if (data.hasExtra("paths")) {
                        //If it is a file selection mode, you need to get the path collection of all the files selected
                        //List<String> list = data.getStringArrayListExtra(Constant.RESULT_INFO);//Constant.RESULT_INFO == "paths"
                        List<String> list = data.getStringArrayListExtra("paths");
                        ToastOrLogUtil.e(TAG, list.toString());
                        dataBinding.setYPath(list.get(0));
                        readInfo(list.get(0), DimenItemBean.TYPE_Y, setYBeen);
                    } else {
                        //If it is a folder selection mode, you need to get the folder path of your choice
                        String path = data.getStringExtra("path");
                        ToastOrLogUtil.e(TAG, path);
                    }
                    break;
            }
        } else {
            ToastOrLogUtil.show(this, "获取路径失败，请重新选择");
        }

    }

    private void readInfo(String path, int type,HashSet<DimenItemBean> setBeen) {
        ArrayList<DimenItemBean> item = ScreenUtil.getItem(path, type,setBeen);
        switch (type) {
            case DimenItemBean.TYPE_X:
                xItemBeen = item;
                break;
            case DimenItemBean.TYPE_Y:
                yItemBeen = item;
                break;
        }
        for (DimenItemBean dimenItemBean : setBeen) {
            Log.e(TAG, "readInfo: setBeen "+dimenItemBean );
        }
        serachAdpater.clear();
        adpater.clear();
        settedAdapter.clear();
        ArrayList<String> list = new ArrayList<>();
        for (DimenItemBean dimenItemBean : item) {
            list.add(dimenItemBean.getName() + " " + dimenItemBean.getValue() + dimenItemBean.getUnit());
        }
        ArrayList<String> list1 = new ArrayList<>();
        for (DimenItemBean dimenItemBean : setBeen) {
            list1.add(getSetKey(dimenItemBean));
        }
        settedAdapter.addAll(list1);
        settedAdapter.notifyDataSetChanged();
        adpater.addAll(list);
        adpater.notifyDataSetChanged();
        serachAdpater.addAll(list);
        serachAdpater.notifyDataSetChanged();
        Log.e(TAG, "readInfo: end");
    }

    @NonNull
    private String getSetKey(DimenItemBean dimenItemBean) {
        return (TextUtils.isEmpty(dimenItemBean.getName())?dimenItemBean.getType():dimenItemBean.getName()) + " " + dimenItemBean.getOprate() + dimenItemBean.getNum();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ArrayList<DimenItemBean> itemBeen;
        if(dataBinding.radio.getCheckedRadioButtonId()==R.id.x){
            itemBeen= xItemBeen;
        }else{
            itemBeen=yItemBeen;
        }
        Toast.makeText(this, itemBeen.get(position).getName() + "  " + itemBeen.get(position).getValue() + itemBeen.get(position).getUnit(), Toast.LENGTH_SHORT).show();
        selectBean = itemBeen.get(position);
        dataBinding.search.setText(((TextView)view).getText());
        dataBinding.setSetEnable(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save:
                if(!TextUtils.isEmpty(num)&&!TextUtils.isEmpty(oprate)) {
                    Log.e(TAG, "onClick: " + oprate + "  ~ " + num);
                    selectBean.setOprate(oprate);
                    selectBean.setNum(Double.valueOf(num));
                    settingBeen.add(selectBean);
                    Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, "数据有误！", Toast.LENGTH_SHORT).show();
                }
                Log.e(TAG, "onClick: "+ settingBeen.size()+" 保存 ："+selectBean );
                break;
            case R.id.xdp:
                unitSet(DimenItemBean.TYPE_XDP, v);
                break;
            case R.id.xsp:
                unitSet(DimenItemBean.TYPE_XSP, v);
                break;
            case R.id.xpx:
                unitSet(DimenItemBean.TYPE_XPX,v);
                break;
            case R.id.ydp:
                unitSet(DimenItemBean.TYPE_YDP,v);
                break;
            case R.id.ypx:
                unitSet(DimenItemBean.TYPE_YPX,v);
                break;
            case R.id.ysp:
                unitSet(DimenItemBean.TYPE_YSP,v);
                break;
            case R.id.back:
                finish();
                break;
            case R.id.run:
                saveSetting();
                break;
            case R.id.showSet:
                dataBinding.showSetLinear.setVisibility(View.VISIBLE);
                break;
            case R.id.dismissShowSet:
                dataBinding.showSetLinear.setVisibility(View.GONE);
                break;
        }
    }

    private void saveSetting() {
        String path = dataBinding.linear1.getVisibility() == View.VISIBLE ? dataBinding.xPath.getText().toString() : dataBinding.yPath.getText().toString();
        if(TextUtils.isEmpty(path)){
            Toast.makeText(this, "请先选择保存文件！", Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuilder db=new StringBuilder();
        db.append("<!--start\n").append(screenBean.getWidth_px()+"*"+screenBean.getHeight_px()+"*"+screenBean.getDpi()+"\n");
        if(dataBinding.linear1.getVisibility()==View.VISIBLE){
            settingBeen.addAll(setXBeen);
            setXBeen.clear();
            setXBeen.addAll(settingBeen);
        }else{
            settingBeen.addAll(setYBeen);
            setYBeen.clear();
            setYBeen.addAll(settingBeen);
        }
        for (DimenItemBean dimenItemBean : settingBeen) {
            switch (dimenItemBean.getType()){
                case DimenItemBean.TYPE_X:
                case DimenItemBean.TYPE_Y:
                    db.append(dimenItemBean.getType()+"/"+dimenItemBean.getName()+"/"+dimenItemBean.getOprate()
                    +"/"+dimenItemBean.getNum()+"\n");
                    break;
                case DimenItemBean.TYPE_XDP:
                case DimenItemBean.TYPE_YDP:
                case DimenItemBean.TYPE_XSP:
                case DimenItemBean.TYPE_YSP:
                case DimenItemBean.TYPE_XPX:
                case DimenItemBean.TYPE_YPX:
                    db.append(dimenItemBean.getType()+"/"+dimenItemBean.getOprate()
                            +"/"+dimenItemBean.getNum()+"\n");
                    break;
            }
        }
        db.append("end-->\n");
        Log.e(TAG, "saveSetting: "+db.toString() );
        Toast.makeText(this, "生成成功", Toast.LENGTH_SHORT).show();
        settedAdapter.clear();
        ArrayList<String> list = new ArrayList<>();
        if(settingBeen!=null) {
            for (DimenItemBean dimenItemBean : settingBeen) {
                list.add(getSetKey(dimenItemBean));
            }
        }
        settedAdapter.addAll(list);
        settedAdapter.notifyDataSetChanged();
        settingBeen.clear();
        ScreenUtil.saveSet(path,
                db);
    }

    private void unitSet(int type,View v) {
        selectBean = new DimenItemBean(type);
        dataBinding.search.setText(((TextView)v).getText());
        dataBinding.setSetEnable(true);
    }

    @Override
    public void onCheckedChanged(final RadioGroup group, @IdRes final int checkedId) {
        Log.e(TAG, "onCheckedChanged: "+checkedId+"   "+R.id.x+"  "+R.id.y );
        if((dataBinding.linear1.getVisibility()==View.VISIBLE&&checkedId==R.id.x)||
                ((dataBinding.linear1.getVisibility()==View.GONE&&checkedId==R.id.y))){
            return;
        }
        if(settingBeen.size()>0){
            AlertDialog alertDialog2 = new AlertDialog.Builder(this)
                    .setTitle("设置还未生成")
                    .setMessage("是否生成设置？")
                    .setIcon(R.mipmap.ic_launcher)
                    .setPositiveButton("取消切换", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            group.check((checkedId==R.id.x)?R.id.y:R.id.x);
                        }
                    })
                    .setNegativeButton("丢弃", new DialogInterface.OnClickListener() {//添加取消
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            settingBeen.clear();
                            setIsSetttingX(checkedId);
                        }
                    })
                    .setNeutralButton("生成", new DialogInterface.OnClickListener() {//添加普通按钮
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            saveSetting();
                            setIsSetttingX(checkedId);
                        }
                    })
                    .create();
            alertDialog2.show();
            return;
        }
        settingBeen.clear();
        setIsSetttingX(checkedId);
    }

    private void setIsSetttingX(@IdRes int checkedId) {
        serachAdpater.clear();
        adpater.clear();
        settedAdapter.clear();
        ArrayList<String> list = new ArrayList<>();
        ArrayList<String> list1 = new ArrayList<>();
        switch (checkedId){
            case R.id.x:
                dataBinding.setIsSettingX(true);
                if(xItemBeen!=null) {
                    for (DimenItemBean dimenItemBean : xItemBeen) {
                        list.add(dimenItemBean.getName() + " " + dimenItemBean.getValue() + dimenItemBean.getUnit());
                    }
                }
                if(setXBeen!=null){
                    for (DimenItemBean dimenItemBean : setXBeen) {
                        list1.add(getSetKey(dimenItemBean));
                    }
                }
                break;
            case R.id.y:
                dataBinding.setIsSettingX(false);
                if(yItemBeen!=null) {
                    for (DimenItemBean dimenItemBean : yItemBeen) {
                        list.add(dimenItemBean.getName() + " " + dimenItemBean.getValue() + dimenItemBean.getUnit());
                    }
                }
                if(setYBeen!=null){
                    for (DimenItemBean dimenItemBean : setYBeen) {
                        list1.add(getSetKey(dimenItemBean));
                    }
                }
                break;
        }
        settedAdapter.addAll(list1);
        settedAdapter.notifyDataSetChanged();
        adpater.addAll(list);
        serachAdpater.addAll(list);
        adpater.notifyDataSetChanged();
        serachAdpater.notifyDataSetChanged();
    }

    /**
     * 长安删除已有设置
     * @param parent
     * @param view
     * @param position
     * @param id
     * @return
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
        AlertDialog alertDialog2 = new AlertDialog.Builder(this)
                .setTitle("删除")
                .setMessage("是否删除该条设置？")
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton("删除", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        TextView textView = (TextView) view;
                        DimenItemBean item=null;
                        HashSet<DimenItemBean> been = null;
                        if(dataBinding.radio.getCheckedRadioButtonId()==R.id.x){
                            been = SpecialSetActivity.this.setXBeen;
                        }else{
                            been = SpecialSetActivity.this.setYBeen;
                        }
                        for (DimenItemBean dimenItemBean : been) {
                            if(textView.getText().toString().equals(getSetKey(dimenItemBean))){
                                item=dimenItemBean;
                                break;
                            }
                        }
                        if(item!=null) {
                            been.remove(item);
                            settedAdapter.clear();
                            ArrayList<String> list1 = new ArrayList<>();
                            for (DimenItemBean dimenItemBean : been) {
                                list1.add(getSetKey(dimenItemBean));
                            }
                            settedAdapter.addAll(list1);
                            settedAdapter.notifyDataSetChanged();
                            saveSetting();
                            Toast.makeText(SpecialSetActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(SpecialSetActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {//添加取消
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create();
        alertDialog2.show();

        return false;
    }
}
