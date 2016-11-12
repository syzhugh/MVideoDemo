package com.sun.mvideodemo.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.blankj.utilcode.utils.ConvertUtils;
import com.sun.mvideodemo.R;


/**
 * Created by Yaozong on 2016/10/5.
 */

public class VolumeTest extends View {

    private int color;
    private int width;
    private int height;
    private Paint paint;

    private Bitmap bitmap, bitmap_none;


    public VolumeTest(Context context) {
        this(context, null);
    }

    public VolumeTest(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VolumeTest(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);


        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.volume_1);
        bitmap_none = BitmapFactory.decodeResource(getResources(), R.drawable.volume_0);
        paint = new Paint();

        width = ConvertUtils.dp2px(context, 40);
        height = width;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /*画图片*/
        RectF rectF1 = new RectF(0, 0, getWidth(), getHeight());
        canvas.drawRoundRect(rectF1, 25, 25, paint);

        paint.setAlpha(0);
        RectF rectFPic = new RectF(
                (getWidth() - width) / 2,
                getHeight() * 0.15f, (getWidth() + width) / 2,
                height + getHeight() * 0.15f);

        paint.reset();
        canvas.drawBitmap(bitmap, null, rectFPic, paint);



        /*line1*/
        Paint paint = new Paint();
        int save1 = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);
        paint.setStrokeWidth(35);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);

        canvas.drawLine(getWidth() * 0.15f,
                rectFPic.bottom + getHeight() * 0.15f,
                getWidth() * 0.85f,
                rectFPic.bottom + getHeight() * 0.15f, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));


        /*line2*/
        paint.setStrokeWidth(32);
        paint.setColor(color);
        canvas.drawLine(getWidth() * 0.15f,
                rectFPic.bottom + getHeight() * 0.15f,
                getWidth() * 0.85f,
                rectFPic.bottom + getHeight() * 0.15f, paint);
        paint.setXfermode(null);
        canvas.restoreToCount(save1);

        paint.setStrokeWidth(25);
        paint.setColor(Color.WHITE);


        float start = getWidth() * 0.15f;
        float end = getWidth() * 0.85f;

        float temp = current * 1.0f / max;
        if (temp <= 0)
            temp = 0f;
        if (temp >= 1)
            temp = 1f;

        /*line3*/
        canvas.drawLine(start,
                rectFPic.bottom + getHeight() * 0.15f,
                start + temp * (end - start),
                rectFPic.bottom + getHeight() * 0.15f, paint);


    }

    private float current = 0;
    private int max = 0;

    public void setMax(int max) {
        this.max = max;
    }

    public float getCurrent() {
        return current;
    }

    public int getMax() {
        return max;
    }

    public void setCurrent(float current) {
        this.current = current;
        invalidate();
    }


}
