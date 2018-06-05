package com.cloudminds.register.repository.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.cloudminds.register.repository.db.entity.EmployeeEntity;

import java.util.List;

import io.reactivex.Flowable;

/**
 * Created
 */

@Dao
public interface EmployeeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(EmployeeEntity employee);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<EmployeeEntity> employees);

    @Query("SELECT * FROM employee WHERE Id =:id")
    EmployeeEntity select(int id);

    @Query("SELECT * FROM employee")
    Flowable<List<EmployeeEntity>> getAllEntity();

    @Query("DELETE FROM employee WHERE Id =:id")
    void deleteEmployee(int id);

}
