package com.example.wqllj.locationshare.db.bean;

import com.baidu.mapapi.model.LatLng;
import com.example.wqllj.locationshare.db.converter.StringConverter;
import com.example.wqllj.locationshare.db.converter.StringConverterLatLng;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;

/**
 * Created by cloud on 2018/9/13.
 */
@Entity
public class CoordinatePointBean {
    @Id(autoincrement = true)
    Long id;
    @Convert(columnType = String.class, converter = StringConverterLatLng.class)
    LatLng latLng;
    Long routeLineId;
    Long date;
    @Generated(hash = 1146125135)
    public CoordinatePointBean(Long id, LatLng latLng, Long routeLineId,
            Long date) {
        this.id = id;
        this.latLng = latLng;
        this.routeLineId = routeLineId;
        this.date = date;
    }
    @Keep
    @Generated
    public CoordinatePointBean( LatLng latLng, @NotNull Long routeLineId,
                               Long date) {
        this.latLng = latLng;
        this.routeLineId = routeLineId;
        this.date = date;
    }
    @Generated(hash = 1747037668)
    public CoordinatePointBean() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public LatLng getLatLng() {
        return this.latLng;
    }
    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }
    public Long getRouteLineId() {
        return this.routeLineId;
    }
    public void setRouteLineId(Long routeLineId) {
        this.routeLineId = routeLineId;
    }
    public Long getDate() {
        return this.date;
    }
    public void setDate(Long date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "CoordinatePointBean{" +
                "id=" + id +
                ", latLng=" + latLng +
                ", routeLineId=" + routeLineId +
                ", date=" + date +
                '}';
    }
}
