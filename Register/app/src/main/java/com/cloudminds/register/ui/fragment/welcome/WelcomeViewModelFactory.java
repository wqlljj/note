package com.cloudminds.register.ui.fragment.welcome;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.cloudminds.register.repository.RegisterRepository;

/**
 * Created
 */

public class WelcomeViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    @NonNull
    private final RegisterRepository mRegisterRepository;

    public WelcomeViewModelFactory(@NonNull RegisterRepository registerRepository) {
        mRegisterRepository = registerRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new WelcomeViewModel(mRegisterRepository);
    }

}
