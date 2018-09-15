package com.example.wqllj.locationshare.db.operator;

import com.baidu.navisdk.ui.routeguide.mapmode.subview.B;
import com.example.wqllj.locationshare.db.DbManager;
import com.example.wqllj.locationshare.db.bean.PersonBean;
import com.example.wqllj.locationshare.db.dao.DaoSession;

import java.lang.reflect.ParameterizedType;
import java.util.List;

/**
 * Created by cloud on 2018/9/11.
 */

public abstract class Operator<Bean extends Object> {
    public  void insertOrReplace(Bean bean){
        DbManager.getInstance().getDaoSession().insertOrReplace(bean);
    }
    public  void delete(Bean bean){
        DbManager.getInstance().getDaoSession().delete(bean);
    }
    public  void delete(){
        Class<Bean> beanClass = (Class<Bean>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        DbManager.getInstance().getDaoSession().deleteAll(beanClass);
    }
    public List<Bean> queryAll(){
        Class<Bean> beanClass = (Class<Bean>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        return DbManager.getInstance().getDaoSession().queryBuilder(beanClass).list();
    }
    public void update(Bean bean){
        DbManager.getInstance().getDaoSession().update(bean);
    }

}
