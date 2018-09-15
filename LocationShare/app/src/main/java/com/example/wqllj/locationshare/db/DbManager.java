package com.example.wqllj.locationshare.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.example.wqllj.locationshare.db.dao.DaoMaster;
import com.example.wqllj.locationshare.db.dao.DaoSession;
import com.example.wqllj.locationshare.db.operator.Operator;

import static org.greenrobot.greendao.test.DbTest.DB_NAME;

/**
 * Created by cloud on 2018/9/6.
 */

public class DbManager {
    static DbManager manager;
    private static DaoMaster.DevOpenHelper mDevOpenHelper;
    private static DaoMaster mDaoMaster;
    private static DaoSession mDaoSession;
    private  Context mContext;

    public DbManager(Context context) {
        this.mContext = context;
        // 初始化数据库信息
        mDevOpenHelper = new DaoMaster.DevOpenHelper(context, DB_NAME);
        getDaoMaster();
        getDaoSession();
    }
    public static void init(Context context){
        if(manager==null) {
            manager = new DbManager(context);

        }
    }
    public static DbManager getInstance(){
        return manager;
    }
    /**
     * 获取可读数据库
     *
     * @return
     */
    public  SQLiteDatabase getReadableDatabase() {
        return mDevOpenHelper.getReadableDatabase();
    }
    /**
     * 获取可写数据库
     *
     * @return
     */
    public  SQLiteDatabase getWritableDatabase() {
        return mDevOpenHelper.getWritableDatabase();

    }
    /**
     * 获取DaoMaster
     *
     * @return
     */
    public  DaoMaster getDaoMaster() {
        if (null == mDaoMaster) {
            synchronized (DbManager.class) {
                if (null == mDaoMaster) {
                    mDaoMaster = new DaoMaster(getWritableDatabase());
                }
            }
        }
        return mDaoMaster;
    }
    /**
     * 获取DaoSession
     *
     * @return
     */
    public  DaoSession getDaoSession() {
        if (null == mDaoSession) {
            synchronized (DbManager.class) {
                mDaoSession = getDaoMaster().newSession();
            }
        }
        return mDaoSession;
    }
    public <Op extends Operator,Bean> Op getOperator(Class<Op> opClass){
        try {
            Op d = opClass.newInstance();
            return d;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
