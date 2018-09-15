package com.example.wqllj.locationshare.db.operator;

import com.example.wqllj.locationshare.db.DbManager;
import com.example.wqllj.locationshare.db.bean.PersonBean;
import com.example.wqllj.locationshare.db.dao.DaoSession;
import com.example.wqllj.locationshare.db.dao.PersonBeanDao;

import java.util.List;

/**
 * Created by cloud on 2018/9/11.
 */

public class PersonOperator extends Operator<PersonBean> {
    public void deleteByKey(Long key){
        DbManager.getInstance().getDaoSession().getPersonBeanDao().deleteByKey(key);
    }
    public PersonBean queryByKey(Long key){
        return DbManager.getInstance().getDaoSession().getPersonBeanDao().load(key);
    }
    public List<PersonBean> queryByName(String name){
        List<PersonBean> list = DbManager.getInstance().getDaoSession().getPersonBeanDao().queryBuilder().where(PersonBeanDao.Properties.Name.eq(name)).list();
        return list;
    }
}
