package com.example.wqllj.locationshare.db.converter;

import com.baidu.mapapi.model.LatLng;

import org.greenrobot.greendao.converter.PropertyConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cloud on 2018/9/10.
 */

public class StringConverterLatLng implements PropertyConverter<LatLng, String> {

    @Override
    public LatLng convertToEntityProperty(String databaseValue) {
        String[] split = databaseValue.split(",");
        return new LatLng(Double.valueOf(split[0]),Double.valueOf(split[1]));
    }

    @Override
    public String convertToDatabaseValue(LatLng entityProperty) {
        return entityProperty.latitude+","+entityProperty.longitude;
    }
}