package com.cloudminds.register.repository.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.cloudminds.register.repository.db.dao.EmployeeDao;
import com.cloudminds.register.repository.db.entity.EmployeeEntity;

/**
 * Created
 */

@Database(entities = {EmployeeEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private volatile static AppDatabase sInstance;

    private static final String DATABASE_NAME = "register.db";

    public static AppDatabase getInstance(final Context context) {
        if (sInstance == null) {
            synchronized (AppDatabase.class) {
                if (sInstance == null) {
                    sInstance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DATABASE_NAME).build();
                }
            }
        }
        return sInstance;
    }

    abstract public EmployeeDao employeeDao();
}
