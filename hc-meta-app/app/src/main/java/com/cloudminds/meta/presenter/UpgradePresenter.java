package com.cloudminds.meta.presenter;

import com.cloudminds.meta.activity.IUpgradeView;
import com.cloudminds.meta.model.UpgradeModel;

/**
 * Created by tiger on 17-4-6.
 */

public class UpgradePresenter{

    private IUpgradeView mView;
    private UpgradeModel mModel;

    public UpgradePresenter(IUpgradeView view){
        this.mView = view;
        mModel = new UpgradeModel();
    }

    public void upgradeMeta(){
        mModel.upgradeMeta();
    }


}
