package com.example.wqllj.locationshare.db.operator;

import com.example.wqllj.locationshare.db.DbManager;
import com.example.wqllj.locationshare.db.bean.CoordinatePointBean;
import com.example.wqllj.locationshare.db.bean.EventBean;
import com.example.wqllj.locationshare.db.bean.RouteLineBean;
import com.example.wqllj.locationshare.db.dao.EventBeanDao;
import com.example.wqllj.locationshare.db.dao.RouteLineBeanDao;

import java.util.Iterator;
import java.util.List;

/**
 * Created by cloud on 2018/9/11.
 */

public class RouteLineOperator extends Operator<RouteLineBean> {
    @Override
    public void insertOrReplace(RouteLineBean routeLineBean) {
        CoordinatePointOperator operator = DbManager.getInstance().getOperator(CoordinatePointOperator.class);
        for (CoordinatePointBean pointBean : routeLineBean.getLatLngs()) {
            operator.insertOrReplace(pointBean);
        }
        super.insertOrReplace(routeLineBean);
    }

    public void deleteByTime(Long time){
        RouteLineBeanDao routeLineBeanDao = DbManager.getInstance().getDaoSession().getRouteLineBeanDao();
        List<RouteLineBean> list =queryByTime(time);
        for (RouteLineBean routeLineBean : list) {
            routeLineBeanDao.delete(routeLineBean);
        }
    }
    public void deleteByName(String name){
        RouteLineBeanDao eventBeanDao = DbManager.getInstance().getDaoSession().getRouteLineBeanDao();
        List<RouteLineBean> list =queryByName(name);
        for (RouteLineBean routeLineBean : list) {
            eventBeanDao.delete(routeLineBean);
        }
    }
    public List<RouteLineBean> queryByPersonId(Long personId){
        return DbManager.getInstance().getDaoSession().getRouteLineBeanDao().queryBuilder().where(RouteLineBeanDao.Properties.PersonId.eq(personId)).list();
    }
    public List<RouteLineBean> queryByTime(Long time){
        return DbManager.getInstance().getDaoSession().getRouteLineBeanDao().queryBuilder().where(RouteLineBeanDao.Properties.Date.eq(time)).list();
    }
    public List<RouteLineBean> queryByName(String name){
        List<RouteLineBean> list = DbManager.getInstance().getDaoSession().getRouteLineBeanDao().queryBuilder().list();
        Iterator<RouteLineBean> iterator = list.iterator();
        while (iterator.hasNext()){
            RouteLineBean lineBean = iterator.next();
            if(!lineBean.getPerson().getName().equals(name)){
                list.remove(lineBean);
            }
        }
        return list;
    }

    public List<RouteLineBean> queryRouteLineByDate(Long start, Long end) {
        List<RouteLineBean> list = DbManager.getInstance().getDaoSession().getRouteLineBeanDao().queryBuilder().where(RouteLineBeanDao.Properties.Date.ge(start)).where(RouteLineBeanDao.Properties.Date.le(end)).list();
        return list;
    }
}
