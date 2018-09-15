package com.example.wqllj.locationshare.db.bean;


import com.baidu.mapapi.model.LatLng;
import com.example.wqllj.locationshare.db.converter.StringConverter;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.ToOne;

import java.util.ArrayList;
import java.util.List;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;
import com.example.wqllj.locationshare.db.dao.DaoSession;
import com.example.wqllj.locationshare.db.dao.PersonBeanDao;
import com.example.wqllj.locationshare.db.dao.RouteLineBeanDao;

/**
 * Created by cloud on 2018/9/6.
 */
@Entity(indexes = {@Index(value = "id ASC",unique = true)})
public class RouteLineBean {
    @Id(autoincrement = true)
    Long id;
    @Convert(columnType = String.class, converter = StringConverter.class)
    List<CoordinatePointBean> latLngs;
    Long date;
    @ToOne(joinProperty = "id")
    PersonBean person;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 82478916)
    private transient RouteLineBeanDao myDao;
    @Generated(hash = 1493031423)
    public RouteLineBean(Long id, List<CoordinatePointBean> latLngs, Long date) {
        this.id = id;
        this.latLngs = latLngs;
        this.date = date;
    }
    public RouteLineBean( List<CoordinatePointBean> latLngs, Long date,PersonBean personBean) {
        this.person=personBean;
        this.latLngs = latLngs;
        this.date = date;
    }
    @Generated(hash = 757748207)
    public RouteLineBean() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public List<CoordinatePointBean> getLatLngs() {
        return this.latLngs;
    }
    public void setLatLngs(List<CoordinatePointBean> latLngs) {
        this.latLngs = latLngs;
    }
    public Long getDate() {
        return this.date;
    }
    public void setDate(Long date) {
        this.date = date;
    }
    @Generated(hash = 1154009267)
    private transient Long person__resolvedKey;
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1409988224)
    public PersonBean getPerson() {
        Long __key = this.id;
        if (person__resolvedKey == null || !person__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            PersonBeanDao targetDao = daoSession.getPersonBeanDao();
            PersonBean personNew = targetDao.load(__key);
            synchronized (this) {
                person = personNew;
                person__resolvedKey = __key;
            }
        }
        return person;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 53676548)
    public void setPerson(PersonBean person) {
        synchronized (this) {
            this.person = person;
            id = person == null ? null : person.getId();
            person__resolvedKey = id;
        }
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 540699559)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getRouteLineBeanDao() : null;
    }

    @Override
    public String toString() {
        return "RouteLineBean{" +
                "id=" + id +
                ", latLngs=" + latLngs +
                ", date=" + date +
                ", person=" + person +
                ", daoSession=" + daoSession +
                ", myDao=" + myDao +
                ", person__resolvedKey=" + person__resolvedKey +
                '}';
    }
}
