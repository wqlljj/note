package com.cloudminds.register.binding;

import android.databinding.BindingAdapter;
import android.view.View;

/**
 * Created
 */

public class BindingAdapters {
    @BindingAdapter("visibleGone")
    public static void showHide(View view, boolean show) {
        view.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
