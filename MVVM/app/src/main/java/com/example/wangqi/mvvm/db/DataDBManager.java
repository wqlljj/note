package com.example.wangqi.mvvm.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.example.wangqi.mvvm.db.bean.UserBean;
import com.example.wangqi.mvvm.db.dao.UserDao;


/**
 * Created by wangqi on 2018/6/1.
 */
@Database(entities = {UserBean.class}, version = 1, exportSchema = false)
public abstract class DataDBManager extends RoomDatabase {
    private static final String DB_NAME = "UserDatabase.db";
    static DataDBManager manager;
    public static synchronized DataDBManager getInstance(Context context) {
        if (manager == null) {
            manager = create(context);
        }
        return manager;
    }
    private static DataDBManager create(final Context context) {
        return Room.databaseBuilder( context, DataDBManager.class, DB_NAME).build();
    }
    public abstract UserDao getUserDao();
}
