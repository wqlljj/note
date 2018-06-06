package com.cloudminds.meta.presenter;

import android.content.Context;

import com.cloudminds.meta.model.FamilyItemAddModel;
import com.cloudminds.meta.activity.IFamilyItemAddView;

/**
 * Created by tiger on 17-4-11.
 */

public class FamilyItemAddPresenter {
    private IFamilyItemAddView mView;
    private FamilyItemAddModel mModel;

    public FamilyItemAddPresenter(IFamilyItemAddView mView){
        this.mView = mView;
        mModel = new FamilyItemAddModel((Context)mView);
    }

}
