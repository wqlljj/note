package com.cloudminds.register.repository;

import android.util.Log;

import com.cloudminds.register.repository.db.AppDatabase;
import com.cloudminds.register.repository.db.dao.EmployeeDao;
import com.cloudminds.register.repository.db.entity.EmployeeEntity;
import com.cloudminds.register.repository.db.entity.EmployeeInfo;
import com.cloudminds.register.repository.network.HttpUtils;
import com.cloudminds.register.repository.network.MQTTManager;
import com.cloudminds.register.repository.network.bean.Admin;
import com.cloudminds.register.repository.network.bean.Department;
import com.cloudminds.register.repository.network.bean.Event;
import com.cloudminds.register.repository.network.bean.Response;
import com.cloudminds.register.repository.network.bean.Visitor;
import com.cloudminds.register.utils.Utils;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.greenrobot.eventbus.EventBus;

import java.util.List;

import io.reactivex.Flowable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Created
 */

public class RegisterRepository {

    private static final String TAG = "RegisterRepository";

    private volatile static RegisterRepository sInstance;

    private final EmployeeDao mEmployeeDao;

    private final MQTTManager mMQTTManager;

    public static RegisterRepository getInstance(AppDatabase appDatabase) {
        if (sInstance == null) {
            synchronized (RegisterRepository.class) {
                if (sInstance == null) {
                    sInstance = new RegisterRepository(appDatabase);
                }
            }
        }
        return sInstance;
    }

    private RegisterRepository(AppDatabase appDatabase) {
        mEmployeeDao = appDatabase.employeeDao();
        mMQTTManager = MQTTManager.getInstance();
    }

    /**
     * Admin login
     *
     * @param mEntity admin entity
     */
    public Flowable<Response> login(Admin mEntity) {
        return HttpUtils.getRequestService().login(mEntity.getPwd());
    }

    /**
     * upload employee info
     *
     * @param entity employee entity
     */
    public Flowable<Response> postEmployee(EmployeeEntity entity) {
        Log.d(TAG, "postEmployee entity = " + entity);

        MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("live", entity.getLive())
                .addFormDataPart("eid", entity.getEid())
                .addFormDataPart("name", entity.getName())
                .addFormDataPart("ename", entity.getEname())
                .addFormDataPart("departmentID", String.valueOf(entity.getDepartmentID()))
                .addFormDataPart("gender", String.valueOf(Utils.genderConvert(entity.getGender())))
                .addFormDataPart("birthday", entity.getBirthday())
                .addFormDataPart("ed", entity.getEd())
                .addFormDataPart("position", entity.getPosition())
                .addFormDataPart("email", entity.getEmail())
                .addFormDataPart("phone", entity.getPhone())
                .addFormDataPart("symbol", entity.getSymbol());

        //update info file maybe null.
        if (entity.getAvatar() != null) {
            requestBody.addFormDataPart("avatar", entity.getAvatar().getName(), RequestBody.create(MediaType.parse("image/*"), entity.getAvatar()));
        }

        //update info
        if (entity.getId() != -1) {
            Log.i(TAG, "postEmployee uploadUpdatedEmployee = " + requestBody.build());
            requestBody.addFormDataPart("id", String.valueOf(entity.getId()));
            return HttpUtils.getRequestService().uploadUpdatedEmployee(requestBody.build());
        }
        Log.i(TAG, "postEmployee new uploadEmployee = " + requestBody.build());
        //upload a new employee info.
        return HttpUtils.getRequestService().uploadEmployee(requestBody.build());
    }

    /**
     * upload visitor info
     *
     * @param entity visitor entity
     */
    public Flowable<Response> postVisitor(Visitor entity) {
        MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("live", entity.getLive())
                .addFormDataPart("name", entity.getName())
                .addFormDataPart("gender", String.valueOf(entity.getGender()))
                .addFormDataPart("company", entity.getCompany())
                .addFormDataPart("visitors", String.valueOf(entity.getVisitors()))
                .addFormDataPart("purpose", entity.getPurpose())
                .addFormDataPart("interviewer", String.valueOf(entity.getInterviewerID()))
                .addFormDataPart("position", entity.getPosition())
                .addFormDataPart("sign", entity.getSign().getName(), RequestBody.create((MediaType.parse("image/*")), entity.getSign()));
        if (entity.getAvatar() != null) {
            requestBody.addFormDataPart("avatar", entity.getAvatar().getName(), RequestBody.create(MediaType.parse("image/*"), entity.getAvatar()));
        } else {
            requestBody.addFormDataPart("avatar", "", RequestBody.create(MediaType.parse("image/*"), ""));
        }
        return HttpUtils.getRequestService().uploadVisitor(requestBody.build());
    }

    /**
     * get all employee info
     *
     * @param type request type ,current always is "all"
     * @return response
     */
    public Flowable<EmployeeInfo> getAllEmployee(String type) {
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("param", type)
                .build();
        return HttpUtils.getRequestService().getAllEmployee(requestBody);
    }

    public Flowable<Department> getAllDepartment() {
        return HttpUtils.getRequestService().getAllDepartment();
    }

    public void initClient(MqttCallback callback) {
        mMQTTManager.init(callback);
    }

    public void disconnectClient() {
        mMQTTManager.disconnect();
    }

    public void updateAllEmployee(List<EmployeeEntity> employees) {
        Log.i(TAG, "updateAllEmployee list = " + employees);
        try {
            mEmployeeDao.insertAll(employees);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateEmployee(EmployeeEntity employee) {
        EmployeeEntity localEmployee = mEmployeeDao.select(employee.getId());
        Log.i(TAG, "localEmployee = " + localEmployee);

        if (!"".equals(employee.getLive())) {
            localEmployee.setLive(employee.getLive());
        }

        if (!"".equals(employee.getEid())) {
            localEmployee.setEid(employee.getEid());
        }

        if (!"".equals(employee.getName())) {
            localEmployee.setName(employee.getName());
        }

        if (!"".equals(employee.getEname())) {
            localEmployee.setEname(employee.getEname());
        }

        if (employee.getDepartmentID() != -1) {
            localEmployee.setDepartmentID(employee.getDepartmentID());
        }

        if (!"".equals(employee.getGender())) {
            localEmployee.setGender(employee.getGender());
        }

        if (!"".equals(employee.getBirthday())) {
            localEmployee.setBirthday(employee.getBirthday());
        }

        if (!"".equals(employee.getEd())) {
            localEmployee.setEd(employee.getEd());
        }

        if (!"".equals(employee.getPhone())) {
            localEmployee.setPhone(employee.getPhone());
        }

        if (!"".equals(employee.getPhotoPath())) {
            localEmployee.setPhotoPath(employee.getPhotoPath());
        }

        if (!"".equals(employee.getPosition())) {
            localEmployee.setPosition(employee.getPosition());
        }

        if (!"".equals(employee.getEmail())) {
            localEmployee.setEmail(employee.getEmail());
        }

        Log.i(TAG, "localEmployee after = " + localEmployee);
        try {
            mEmployeeDao.insert(localEmployee);
        } catch (Exception e) {
            e.printStackTrace();
        }
        EventBus.getDefault().post(new Event<EmployeeEntity>(1,localEmployee));
    }

    public void insertEmployee(EmployeeEntity employee) {
        try {
            mEmployeeDao.insert(employee);
        } catch (Exception e) {
            e.printStackTrace();
        }
        EventBus.getDefault().post(new Event<Object>(1,employee.getId()));
    }

    public void deleteEmployee(int id) {
        try {
            EmployeeEntity entity = mEmployeeDao.select(id);
            entity.setLive("No");
            mEmployeeDao.insert(entity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        EventBus.getDefault().post(new Event<Object>(1,id));
    }

    public Flowable<List<EmployeeEntity>> getEmployeeListFromDB() {
        return mEmployeeDao.getAllEntity();
    }
}
