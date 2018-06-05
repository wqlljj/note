package com.cloudminds.meta.accesscontroltv.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.Interpolator;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by WQ on 2018/4/16.
 */

public class CustomRecyclerView extends RecyclerView {

    private String TAG="APP/CustomRecyclerView";

    public CustomRecyclerView(Context context) {
        super(context);
    }

    public CustomRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public void smoothScrollBy(int dx, int dy) {
        this.smoothScrollBy(dx, dy, (Interpolator)null);
    }

    public void smoothScrollBy(int dx, int dy, Interpolator interpolator) {
        Class<?> aClass = this.getClass().getSuperclass();
        try {
            if(interpolator==null){

                Field sQuinticInterpolatorField = aClass.getDeclaredField("sQuinticInterpolator");
                sQuinticInterpolatorField.setAccessible(true);
                interpolator = (Interpolator) sQuinticInterpolatorField.get(this);
            }
            Field mLayoutField = aClass.getDeclaredField("mLayout");
            mLayoutField.setAccessible(true);
            RecyclerView.LayoutManager mLayout = ((RecyclerView.LayoutManager) mLayoutField.get(this));
            Field mLayoutFrozenField = aClass.getDeclaredField("mLayoutFrozen");
            mLayoutFrozenField.setAccessible(true);
            boolean mLayoutFrozen = mLayoutFrozenField.getBoolean(this);
            if(mLayout==null){
                Log.e("RecyclerView", "Cannot smooth scroll without a LayoutManager set. Call setLayoutManager with a non-null argument.");
            }else if(!mLayoutFrozen){
                if(!mLayout.canScrollHorizontally()) {
                    dx = 0;
                }

                if(!mLayout.canScrollVertically()) {
                    dy = 0;
                }

                if(dx != 0 || dy != 0) {
                    Field mViewFlingerField = aClass.getDeclaredField("mViewFlinger");
                    mViewFlingerField.setAccessible(true);
                    Object mViewFlinger = mViewFlingerField.get(this);
                    Class<?>[] declaredClasses = aClass.getDeclaredClasses();
                    for (Class<?> declaredClass : declaredClasses) {
                        Log.e(TAG, "smoothScrollBy:className = "+declaredClass.getSimpleName() );
                        if(declaredClass.getSimpleName().equals("ViewFlinger")){
                            Method smoothScrollByMethod = declaredClass.getDeclaredMethod("smoothScrollBy", int.class, int.class, int.class, Interpolator.class);
                            smoothScrollByMethod.invoke(mViewFlinger,dx, dy, 2000,interpolator);
                            break;
                        }
                    }
                }
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
