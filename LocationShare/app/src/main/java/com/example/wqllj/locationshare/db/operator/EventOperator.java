package com.example.wqllj.locationshare.db.operator;

import com.example.wqllj.locationshare.db.DbManager;
import com.example.wqllj.locationshare.db.bean.EventBean;
import com.example.wqllj.locationshare.db.bean.PersonBean;
import com.example.wqllj.locationshare.db.dao.EventBeanDao;
import com.example.wqllj.locationshare.db.dao.PersonBeanDao;

import java.util.Iterator;
import java.util.List;

/**
 * Created by cloud on 2018/9/11.
 */

public class EventOperator extends Operator<EventBean> {
    public void deleteByTime(Long time){
        EventBeanDao eventBeanDao = DbManager.getInstance().getDaoSession().getEventBeanDao();
        List<EventBean> list =queryByTime(time);
        for (EventBean eventBean : list) {
            eventBeanDao.delete(eventBean);
        }
    }
    public void deleteByName(String name){
        EventBeanDao eventBeanDao = DbManager.getInstance().getDaoSession().getEventBeanDao();
        List<EventBean> list =queryByName(name);
        for (EventBean eventBean : list) {
            eventBeanDao.delete(eventBean);
        }
    }
    public List<EventBean> queryByTime(Long time){
        List<EventBean> list = DbManager.getInstance().getDaoSession().getEventBeanDao().queryBuilder().list();
        Iterator<EventBean> iterator = list.iterator();
        while (iterator.hasNext()){
            EventBean next = iterator.next();
            if(next.getPointBean().getDate()!=time){
                list.remove(next);
            }
        }
        return list;
    }
    public List<EventBean> queryByName(String name){
        List<EventBean> list = DbManager.getInstance().getDaoSession().getEventBeanDao().queryBuilder().where(EventBeanDao.Properties.Name.eq(name)).list();
        return list;
    }

    public List<EventBean> queryEventByDate(Long start, Long end) {
        List<EventBean> list = DbManager.getInstance().getDaoSession().getEventBeanDao().queryBuilder().list();
        Iterator<EventBean> iterator = list.iterator();
        while (iterator.hasNext()){
            EventBean next = iterator.next();
            if(next.getPointBean().getDate()<start||next.getPointBean().getDate()>end){
                list.remove(next);
            }
        }
        return list;
    }
}
