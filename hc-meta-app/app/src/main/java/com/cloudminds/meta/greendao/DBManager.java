package com.cloudminds.meta.greendao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.cloudminds.meta.bean.FamilyItemBean;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * Created by SX on 2017/8/1.
 */

public class DBManager {
    private String TAG="DBManager";
    private final static String dbName = "meta_db";
    private static DBManager manager;
    private DaoMaster.DevOpenHelper openHelper;
    private Context context;

    private DBManager(Context context) {
        this.context = context;
    }
    public static DBManager getInstance(Context context){
        if (manager == null) {
            synchronized (DBManager.class) {
                if (manager == null) {
                    manager = new DBManager(context);
                }
            }
        }
        return manager;
    }
    private SQLiteDatabase getReadableDatabase() {
        if (openHelper == null) {
            openHelper = new DaoMaster.DevOpenHelper(context, dbName, null);
        }
        SQLiteDatabase db = openHelper.getReadableDatabase();
        return db;
    }
    private SQLiteDatabase getWritableDatabase() {
        if (openHelper == null) {
            openHelper = new DaoMaster.DevOpenHelper(context, dbName, null);
        }
        SQLiteDatabase db = openHelper.getWritableDatabase();
        return db;
    }

    public void insertFamilyItemBean(FamilyItemBean familyItemBean) {
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        FamilyItemBeanDao familyItemBeanDao = daoSession.getFamilyItemBeanDao();
        familyItemBeanDao.insertOrReplace(familyItemBean);
    }
    public void insertFamilyItemBeanList(List<FamilyItemBean> familyItemBeanList) {
        if (familyItemBeanList == null || familyItemBeanList.isEmpty()) {
            return;
        }
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        FamilyItemBeanDao familyItemBeanDao = daoSession.getFamilyItemBeanDao();
        familyItemBeanDao.insertOrReplaceInTx(familyItemBeanList);
    }
    public void deleteFamilyItemBean(FamilyItemBean familyItemBean) {
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        FamilyItemBeanDao familyItemBeanDao= daoSession.getFamilyItemBeanDao();
//        familyItemBeanDao.delete(familyItemBean);
        familyItemBeanDao.deleteByKey(familyItemBean.getFace_id());
    }
    public void deleteFamilyItemBeanAll() {
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        FamilyItemBeanDao familyItemBeanDao= daoSession.getFamilyItemBeanDao();
        familyItemBeanDao.deleteAll();
    }
    public void updateFamilyItemBean(FamilyItemBean familyItemBean) {
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        FamilyItemBeanDao familyItemBeanDao = daoSession.getFamilyItemBeanDao();
        familyItemBeanDao.update(familyItemBean);
    }

    public List<FamilyItemBean> queryFamilyItemBeanList() {
        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        FamilyItemBeanDao familyItemBeanDao = daoSession.getFamilyItemBeanDao();
        QueryBuilder<FamilyItemBean> qb = familyItemBeanDao.queryBuilder();
        List<FamilyItemBean> list=qb.orderDesc(FamilyItemBeanDao.Properties.Face_id).list();
        return list;
    }
}
