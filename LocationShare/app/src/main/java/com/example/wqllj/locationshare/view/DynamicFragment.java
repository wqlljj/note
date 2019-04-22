package com.example.wqllj.locationshare.view;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.wqllj.locationshare.R;
import com.example.wqllj.locationshare.databinding.FragmentDynamicBinding;
import com.example.wqllj.locationshare.model.baidumap.navi_bike_wake.BNaviMainActivity;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link DynamicFragment} factory method to
 * create an instance of this fragment.
 */
public class DynamicFragment extends Fragment implements View.OnClickListener, View.OnTouchListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FragmentDynamicBinding dataBinding;


    public DynamicFragment() {
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
    public static DynamicFragment newInstance(String param1, String param2) {
        DynamicFragment fragment = new DynamicFragment();
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
        dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_dynamic,container,false);
        initView();
        return dataBinding.getRoot();
    }

    private void initView() {
        dataBinding.hFootPrint.setOnClickListener(footPrintClickListener);
        dataBinding.myFootPrint.setOnClickListener(footPrintClickListener);
        dataBinding.navigate.setOnClickListener(this);
        dataBinding.hAction.setOnClickListener(actionClickListener);
        dataBinding.myAction.setOnClickListener(actionClickListener);
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
    View.OnClickListener footPrintClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), FootPrintDetailActivity.class);
            switch (v.getId()) {
                case R.id.h_footPrint:
                    intent.putExtra(FootPrintDetailActivity.KEY_START, System.currentTimeMillis() - 12 * 3600 * 1000);
                    intent.putExtra(FootPrintDetailActivity.KEY_END, System.currentTimeMillis());
                    intent.putExtra(FootPrintDetailActivity.KEY_PERSON_ID, 1l);
                    break;
                case R.id.my_footPrint:
                    intent.putExtra(FootPrintDetailActivity.KEY_START, System.currentTimeMillis() - 12 * 3600 * 1000);
                    intent.putExtra(FootPrintDetailActivity.KEY_END, System.currentTimeMillis());
                    intent.putExtra(FootPrintDetailActivity.KEY_PERSON_ID, 1l);
                    break;
            }
            startActivity(intent);
        }
    };
    View.OnClickListener actionClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), ActionActivity.class);
            intent.putExtra(ActionActivity.KEY_PERSONID,1l);
            startActivity(intent);
        }
    };
    @Override
    public void onClick(View v) {
        switch (v.getId()){
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
