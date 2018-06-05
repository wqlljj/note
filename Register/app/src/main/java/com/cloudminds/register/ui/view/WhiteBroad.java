package com.cloudminds.register.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.cloudminds.register.R;
import com.cloudminds.register.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class WhiteBroad extends View {

    private Path mPath;
    private Canvas mCanvas;
    private Bitmap mBitmap;
    private Paint mPaint;
    private float mX;//start point
    private float mY;
    private Bitmap mBackgroundBitmap;

    private float mSignPadding;

    public WhiteBroad(Context context) {
        this(context, null);
    }

    public WhiteBroad(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);

        TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.WhiteBroad,
                0, 0);
        try {
            mSignPadding = a.getFloat(R.styleable.WhiteBroad_sign_padding_start, 150);
        } finally {
            a.recycle();
        }
        init();
    }

    public WhiteBroad(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPath = new Path();
        setFocusable(true);
        setFocusableInTouchMode(true);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(10);
        mPaint.setColor(Color.BLACK);

        TextPaint textPain = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        textPain.setHinting(Paint.HINTING_ON);
        textPain.setColor(Color.BLACK);
        textPain.setTextSize(66);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBackgroundBitmap = decodeSampledBitmapFromDrawable(getResources(), R.drawable.sign_file, getWidth(), getHeight());
        mBitmap = mBackgroundBitmap.copy(Bitmap.Config.ARGB_8888, true);
        mCanvas = new Canvas(mBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int bgColor = Color.WHITE;
        canvas.drawColor(bgColor);
        canvas.drawBitmap(mBitmap, mSignPadding, slideLength, mPaint);
    }

    boolean isSlide = true;
    float slideLength;
    float mLastY;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX() - mSignPadding;
        float y = event.getY() - slideLength;
        if (!isSlide) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchStart(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touchMove(mX, mY, x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touchUp(x, y);
                    invalidate();
                    break;
                default:
                    break;
            }
        } else {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mLastY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float moveY = event.getY();
                    float distance = moveY - mLastY;
                    mLastY = moveY;
                    slideLength += distance;
                    if (slideLength >= 0) {
                        slideLength = 0;
                    }
                    if (slideLength <= (-1) * (mBitmap.getHeight() - getHeight())) {
                        slideLength = (-1) * (mBitmap.getHeight() - getHeight());
                        isSlide = false;
                    }
                    postInvalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    private void touchMove(float oldX, float oldY, float newX, float newY) {
        //mPath.reset();
        //mPath.moveTo(oldX, oldY);
        mPath.quadTo(oldX, oldY, (newX + oldX) / 2, (newY + oldY) / 2);
        mX = newX;
        mY = newY;

        /*mPath.quadTo(oldX, oldY, newX, newY);
        mX = newX;
        mY = newY;*/
        mCanvas.drawPath(mPath, mPaint);
        //mPath.reset();
    }

    private void touchUp(float x, float y) {
        mCanvas.drawPath(mPath, mPaint);
        //mPath.reset();
    }

    private void touchStart(float x, float y) {
        //mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    public void clean() {
        mBitmap = mBackgroundBitmap.copy(Bitmap.Config.ARGB_8888, true);
        mCanvas.setBitmap(mBitmap);
        mPath.reset();
        mCanvas.drawPath(mPath, mPaint);
        invalidate();
    }

    public boolean isEmpty() {
        return mPath.isEmpty();
    }

    public void save(/*@NonNull String saveDir, @NonNull String saveName*/) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        File outFile = Utils.getSignImage();//new File(saveDir, saveName);
        FileOutputStream outPut = null;
        try {
            outFile.deleteOnExit();
            outPut = new FileOutputStream(outFile);
            out.writeTo(outPut);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outPut != null) {
                try {
                    outPut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static Bitmap decodeSampledBitmapFromDrawable(Resources resources,
                                                          int resId, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = Utils.calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(resources, resId, options);
    }
}
