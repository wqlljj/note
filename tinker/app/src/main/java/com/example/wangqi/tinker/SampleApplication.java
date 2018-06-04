package com.example.wangqi.tinker;

import com.tencent.tinker.loader.app.TinkerApplication;
import com.tencent.tinker.loader.shareutil.ShareConstants;

/**
 * Created by wangqi on 2018/5/30.
 */

public class SampleApplication extends TinkerApplication {
    public SampleApplication() {
        super(ShareConstants.TINKER_ENABLE_ALL, "com.example.wangqi.tinker.SampleApplicationLike",
                "com.tencent.tinker.loader.TinkerLoader", false);
    }

}
