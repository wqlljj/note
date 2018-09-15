package com.example.wqllj.locationshare.view;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.baidu.mapapi.bikenavi.BikeNavigateHelper;
import com.baidu.mapapi.bikenavi.params.BikeNaviLaunchParam;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.walknavi.WalkNavigateHelper;
import com.baidu.mapapi.walknavi.params.WalkNaviLaunchParam;
import com.example.wqllj.locationshare.R;
import com.example.wqllj.locationshare.databinding.FragmentFootPrintBinding;
import com.example.wqllj.locationshare.model.baidumap.navi_bike_wake.BNaviMainActivity;

import java.lang.reflect.Field;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link FootPrintFragment} factory method to
 * create an instance of this fragment.
 */
public class FootPrintFragment extends Fragment implements View.OnClickListener, View.OnTouchListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FragmentFootPrintBinding dataBinding;


    public FootPrintFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MessageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FootPrintFragment newInstance(String param1, String param2) {
        FootPrintFragment fragment = new FootPrintFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_foot_print,container,false);
        initView();
        return dataBinding.getRoot();
    }

    private void initView() {
        dataBinding.hFootPrint.setOnClickListener(this);
        dataBinding.myFootPrint.setOnClickListener(this);
        dataBinding.navigate.setOnClickListener(this);
        dataBinding.action.setOnClickListener(this);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.h_footPrint:
                startActivity(new Intent(getActivity(),FootPrintDetailActivity.class));
                break;
            case R.id.my_footPrint:
                startActivity(new Intent(getActivity(),FootPrintDetailActivity.class));
                break;
            case R.id.navigate:
                startActivity(new Intent(getActivity(),BNaviMainActivity.class));
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Toast.makeText(getContext(), "点击", Toast.LENGTH_SHORT).show();

        switch (v.getId()){
            case R.id.h_footPrint:
//                startActivity(new Intent(getActivity(),MapActivity.class));
                break;
        }
        return false;
    }
}
