package com.cloudminds.meta.customview;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.cloudminds.meta.R;


public class MyTextView2 extends TextView {
	private Paint mPaint;
	private Paint selectmPaint;
	private int count;
	private int width_2;
	private int height_2;
	float radius;
	float gap;
	public MyTextView2(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		count = 0;
		radius = 15;
		gap = radius*2+10;
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setColor(getResources().getColor(R.color.drawer_gray));
		selectmPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		selectmPaint.setColor(getResources().getColor(R.color.common_text_color));
	}


	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		String text = this.getText().toString().trim();
		if(text.equals("")){
			width_2 = this.getWidth()/2;
			height_2 = this.getHeight()/2;
			switch (count) {
			case 0:
				canvas.drawCircle(width_2-gap, height_2+radius, radius, selectmPaint);
				canvas.drawCircle(width_2, height_2+radius, radius, mPaint);
				canvas.drawCircle(width_2+gap, height_2+radius, radius, mPaint);
				break;
			case 1:
				canvas.drawCircle(width_2-gap, height_2+radius, radius, mPaint);
				canvas.drawCircle(width_2, height_2+radius, radius, selectmPaint);
				canvas.drawCircle(width_2+gap, height_2+radius, radius, mPaint);
				break;
			case 2:
				canvas.drawCircle(width_2-gap, height_2+radius, radius, mPaint);
				canvas.drawCircle(width_2, height_2+radius, radius, mPaint);
				canvas.drawCircle(width_2+gap, height_2+radius, radius, selectmPaint);
				break;
			default:
				break;
			}
			count =(++count)%3;
			postInvalidateDelayed(300);
		}
	
	}

}