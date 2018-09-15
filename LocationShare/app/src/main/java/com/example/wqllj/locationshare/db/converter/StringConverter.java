package com.example.wqllj.locationshare.db.converter;

import com.baidu.mapapi.model.LatLng;
import com.example.wqllj.locationshare.db.bean.CoordinatePointBean;

import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.converter.PropertyConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by cloud on 2018/9/10.
 */

public class StringConverter implements PropertyConverter<List<CoordinatePointBean>, String> {

    @Override
    public List<CoordinatePointBean> convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        } else {
            List<CoordinatePointBean> points = new ArrayList<>();
            String[] split = databaseValue.split(";");
            for (String s : split) {
                String[] item = s.split(",");
                if(item.length==5) {
                    points.add(new CoordinatePointBean(Long.valueOf(item[0]), new LatLng(Double.valueOf(item[3]), Double.valueOf(item[4])), Long.valueOf(item[1]), Long.valueOf(item[2])));
                }
            }
            return points;
        }
    }

    @Override
    public String convertToDatabaseValue(List<CoordinatePointBean> entityProperty) {
        if (entityProperty == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (CoordinatePointBean pointBean : entityProperty) {
            sb.append(pointBean.getId())
                    .append(",").append(pointBean.getRouteLineId())
                    .append(",").append(pointBean.getDate())
                    .append(",").append(pointBean.getLatLng().latitude)
                    .append(",").append(pointBean.getLatLng().longitude)
                    .append(";");
        }
        return sb.toString();
    }
}