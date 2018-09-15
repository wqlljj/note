package com.example.wqllj.locationshare.db.bean;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;
import com.example.wqllj.locationshare.db.dao.DaoSession;
import com.example.wqllj.locationshare.db.dao.PersonBeanDao;

/**
 * Created by cloud on 2018/9/6.
 * tableName = "tb_student",//定义表名
 indices = @Index(value = {"name", "sex"}, unique = true),//定义索引
 foreignKeys = {@ForeignKey(entity = ClassEntity.class,
parentColumns = "id",
childColumns = "class_id")}
 */
@Entity()
public class PersonBean {
    @Id(autoincrement = true)
    Long id;
    String name;
    //0男，1女
    int sex;
    @ToOne(joinProperty = "id")
    PersonBean grilFriend;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 731664257)
    private transient PersonBeanDao myDao;
    @Generated(hash = 1381213074)
    public PersonBean(Long id, String name, int sex) {
        this.id = id;
        this.name = name;
        this.sex = sex;
    }
    @Generated(hash = 836535228)
    public PersonBean() {
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
    public int getSex() {
        return this.sex;
    }
    public void setSex(int sex) {
        this.sex = sex;
    }
    @Generated(hash = 1717596003)
    private transient Long grilFriend__resolvedKey;
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1421275251)
    public PersonBean getGrilFriend() {
        Long __key = this.id;
        if (grilFriend__resolvedKey == null
                || !grilFriend__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            PersonBeanDao targetDao = daoSession.getPersonBeanDao();
            PersonBean grilFriendNew = targetDao.load(__key);
            synchronized (this) {
                grilFriend = grilFriendNew;
                grilFriend__resolvedKey = __key;
            }
        }
        return grilFriend;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1090970353)
    public void setGrilFriend(PersonBean grilFriend) {
        synchronized (this) {
            this.grilFriend = grilFriend;
            id = grilFriend == null ? null : grilFriend.getId();
            grilFriend__resolvedKey = id;
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
    @Generated(hash = 1440948169)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getPersonBeanDao() : null;
    }

    @Override
    public String toString() {
        return "PersonBean{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sex=" + sex +
                ", grilFriend=" + grilFriend +
                ", daoSession=" + daoSession +
                ", myDao=" + myDao +
                ", grilFriend__resolvedKey=" + grilFriend__resolvedKey +
                '}';
    }
}
