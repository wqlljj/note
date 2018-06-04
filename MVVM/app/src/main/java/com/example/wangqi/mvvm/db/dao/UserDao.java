package com.example.wangqi.mvvm.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;


import com.example.wangqi.mvvm.db.bean.UserBean;

import java.util.List;

/**
 * Created by wangqi on 2018/6/1.
 */
@Dao
public interface UserDao {
    @Query("SELECT * FROM user_table WHERE id =:id")
    UserBean getUserByid(int id);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserBean userBean);
    @Update
    void update(UserBean userBean);
    @Delete
    void delete(UserBean userBean);
    @Query("SELECT * FROM user_table")
    List<UserBean> getUsers();
}
