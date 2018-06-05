package com.cloudminds.register.utils;

import android.content.Context;

import com.cloudminds.register.repository.RegisterRepository;
import com.cloudminds.register.repository.db.AppDatabase;
import com.cloudminds.register.ui.MainViewModelFactory;
import com.cloudminds.register.ui.fragment.admin.AdminViewModelFactory;
import com.cloudminds.register.ui.fragment.employee.EmployeeViewModelFactory;
import com.cloudminds.register.ui.fragment.visitor.VisitorViewModelFactory;
import com.cloudminds.register.ui.fragment.welcome.WelcomeViewModelFactory;

/**
 * Created
 */

public class InjectorUtils {

    private static RegisterRepository provideRepository(Context context) {
        AppDatabase appDatabase = AppDatabase.getInstance(context.getApplicationContext());
        return RegisterRepository.getInstance(appDatabase);
    }

    public static AdminViewModelFactory provideAdminViewModelFactory(Context context) {
        RegisterRepository repository = provideRepository(context.getApplicationContext());
        return new AdminViewModelFactory(repository);
    }

    public static EmployeeViewModelFactory provideEmployeeViewModelFactory(Context context) {
        RegisterRepository repository = provideRepository(context.getApplicationContext());
        return new EmployeeViewModelFactory(repository);
    }

    public static VisitorViewModelFactory provideVisitorViewModelFactory(Context context) {
        RegisterRepository repository = provideRepository(context.getApplicationContext());
        return new VisitorViewModelFactory(repository);
    }

    public static WelcomeViewModelFactory provideWelcomeViewModelFactory(Context context) {
        RegisterRepository repository = provideRepository(context.getApplicationContext());
        return new WelcomeViewModelFactory(repository);
    }

    public static MainViewModelFactory provideMainViewModelFactory(Context context) {
        RegisterRepository repository = provideRepository(context.getApplicationContext());
        return new MainViewModelFactory(repository);
    }
}
