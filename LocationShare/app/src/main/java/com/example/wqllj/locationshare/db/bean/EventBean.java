package com.example.wqllj.locationshare.db.bean;


import com.baidu.mapapi.model.LatLng;
import com.example.wqllj.locationshare.util.DateUtil;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.DaoException;
import com.example.wqllj.locationshare.db.dao.DaoSession;
import com.example.wqllj.locationshare.db.dao.CoordinatePointBeanDao;
import com.example.wqllj.locationshare.db.dao.EventBeanDao;

/**
 * Created by cloud on 2018/9/6.
 */
@Entity
public class EventBean {
    @Id(autoincrement = true)
    Long id;
    String name;
    int action;
    String data;
    Long person_id;
    Long coordinatePointId;
    @ToOne(joinProperty = "coordinatePointId")
    CoordinatePointBean pointBean;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 519173234)
    private transient EventBeanDao myDao;
    @Generated(hash = 1340844942)
    public EventBean(Long id, String name, int action, String data, Long person_id,
            Long coordinatePointId) {
        this.id = id;
        this.name = name;
        this.action = action;
        this.data = data;
        this.person_id = person_id;
        this.coordinatePointId = coordinatePointId;
    }
    @Keep
    @Generated
    public EventBean(String name, int action, String data, Long person_id,
                     Long coordinatePointId,CoordinatePointBean bean) {
        this.name = name;
        this.action = action;
        this.data = data;
        this.person_id = person_id;
        this.coordinatePointId = coordinatePointId;
        this.pointBean=bean;
    }
    @Generated(hash = 1783294599)
    public EventBean() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getAction() {
        return this.action;
    }
    public void setAction(int action) {
        this.action = action;
    }
    public String getData() {
        return this.data;
    }
    public void setData(String data) {
        this.data = data;
    }
    public Long getPerson_id() {
        return this.person_id;
    }
    public void setPerson_id(Long person_id) {
        this.person_id = person_id;
    }
    public Long getCoordinatePointId() {
        return this.coordinatePointId;
    }
    public void setCoordinatePointId(Long coordinatePointId) {
        this.coordinatePointId = coordinatePointId;
    }
    @Generated(hash = 1781729950)
    private transient Long pointBean__resolvedKey;
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 137650738)
    public CoordinatePointBean getPointBean() {
        Long __key = this.coordinatePointId;
        if (pointBean__resolvedKey == null
                || !pointBean__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            CoordinatePointBeanDao targetDao = daoSession
                    .getCoordinatePointBeanDao();
            CoordinatePointBean pointBeanNew = targetDao.load(__key);
            synchronized (this) {
                pointBean = pointBeanNew;
                pointBean__resolvedKey = __key;
            }
        }
        return pointBean;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 599389492)
    public void setPointBean(CoordinatePointBean pointBean) {
        synchronized (this) {
            this.pointBean = pointBean;
            coordinatePointId = pointBean == null ? null : pointBean.getId();
            pointBean__resolvedKey = coordinatePointId;
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
    @Generated(hash = 1741896799)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getEventBeanDao() : null;
    }

    @Override
    public String toString() {
        return "EventBean{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", action=" + action +
                ", data='" + data + '\'' +
                ", person_id=" + person_id +
                ", coordinatePointId=" + coordinatePointId +
                ", pointBean=" + pointBean +
                ", daoSession=" + daoSession +
                ", myDao=" + myDao +
                ", pointBean__resolvedKey=" + pointBean__resolvedKey +
                '}';
    }
}
