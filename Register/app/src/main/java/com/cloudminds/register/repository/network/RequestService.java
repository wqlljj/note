package com.cloudminds.register.repository.network;

import com.cloudminds.register.repository.db.entity.EmployeeInfo;
import com.cloudminds.register.repository.network.bean.Department;
import com.cloudminds.register.repository.network.bean.Response;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created
 */

public interface RequestService {

    /**
     * Admin login
     *
     * @param password admin's password
     * @return response
     */
    @FormUrlEncoded
    @POST("pwd/chkpower")
    Flowable<Response> login(@Field("pwd") String password);

    /**
     * Get department information
     *
     * @return department
     */
    @GET("department/getall")
    Flowable<Department> getAllDepartment();

    /**
     * Upload employee information
     *
     * @param Body employee information
     * @return response
     */
    @POST("employee/post")
    Flowable<Response> uploadEmployee(@Body RequestBody Body);

    /**
     * Upload visitor information
     *
     * @param body visitor information
     * @return response
     */
    @POST("customer/post")
    Flowable<Response> uploadVisitor(@Body RequestBody body);

    /**
     * get all employee info
     *
     * @param type request type ,current always is "all"
     * @return response
     */
    @POST("employee/getall")
    Flowable<EmployeeInfo> getAllEmployee(@Body RequestBody type);

    /**
     * Upload updated employee info
     *
     * @param Body employee information
     * @return response
     */
    @POST("employee/doupdate")
    Flowable<Response> uploadUpdatedEmployee(@Body RequestBody Body);
}
