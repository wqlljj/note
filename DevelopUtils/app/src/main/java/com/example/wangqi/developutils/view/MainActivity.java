package com.example.wangqi.developutils.view;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.wangqi.developutils.R;
import com.example.wangqi.developutils.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setOnClick(this);
    }
    public void click(View view){
        switch (view.getId()){
            case R.id.screenUtil:
                Toast.makeText(this, "跳转screenUtil", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this,ScreenUtilActivity.class));
                break;
        }
    }

}
