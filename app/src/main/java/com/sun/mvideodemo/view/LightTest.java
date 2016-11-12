package com.sun.mvideodemo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Yaozong on 2016/10/5.
 */

public class LightTest extends View {
    /*工具*/
    private Paint paint;

    /*属性*/

    private float center_x;
    private float center_y;


    public LightTest(Context context) {
        this(context, null);
    }

    public LightTest(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public LightTest(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
//        px = ConvertUtils.dp2px(context, 100f);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /*画中心圆形*/
        center_x = getWidth() / 2;
        center_y = getHeight() / 2;
//        radius = 360;
//        current = 270;

        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(center_x, center_y, getWidth() / 8, paint);

        /*画弧线*/
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);

        Path path = new Path();

        path.addCircle(center_x, center_y, getWidth() * 0.42f, Path.Direction.CW);


        PathMeasure measure = new PathMeasure(path, false);

        paint.reset();
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(30);
        paint.setColor(Color.WHITE);
        paint.setStrokeCap(Paint.Cap.ROUND);

        int num = current / 45;
        float[][] pos = new float[8][2];
        for (int i = 0; i < pos.length; i++) {
            measure.getPosTan((1f / 8f) * i * measure.getLength(), pos[i], null);
        }
        rank(pos, 2);

        for (int i = 0; i < num; i++) {
            canvas.drawLine(pos[i][0], pos[i][1],
                    (pos[i][0] + center_x) / 2, (pos[i][1] + center_y) / 2, paint);
        }

    }

    private void rank(float[][] pos, int time) {
        while (time > 0) {
            float[] temp = pos[pos.length - 1];
            for (int i = pos.length - 1; i > 0; i--) {
                pos[i] = pos[i - 1];
            }
            pos[0] = temp;
            time--;
        }
    }


    int radius;
    int current;

    public void init(int current) {
        this.radius = 360;
        this.current = current;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
        invalidate();
    }
}
