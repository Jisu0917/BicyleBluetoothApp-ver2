package com.activerecycle.tripgauge;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.Nullable;

public class SpeedGraph extends View {
    public SpeedGraph(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    int speed;
    int sweepAngle;
    int maxAngle;

    boolean btconnect = false;

    @Override
    protected void onDraw(android.graphics.Canvas canvas) {
        super.onDraw(canvas);

        if (speed == 99) {
            // 블루투스 연결 안 된 상태를 나타냄
            sweepAngle = 3;
            btconnect = false;
        } else {
            btconnect = true;
            sweepAngle = 260 * speed / 30;
        }
        maxAngle = 260 * 25 / 30;


        //핸드폰 화면크기 가져오기
        DisplayMetrics metrics = new DisplayMetrics();
        getDisplay().getMetrics(metrics);
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        setBackgroundColor(Color.BLACK);

        Paint pnt_gray = new Paint();
        pnt_gray.setStrokeWidth(6f);
        pnt_gray.setColor(Color.GRAY);
        pnt_gray.setStyle(Paint.Style.STROKE);

        RectF rect = new RectF();
        rect = new RectF();
        int full_length = screenWidth * 75 / 100;
        rect.set(screenWidth/2 - 400, 30, screenWidth/2 + 400, 780);
        canvas.drawArc(rect, 140, 260, false, pnt_gray);

        if (!btconnect) {
            Paint pnt_red = new Paint();
            pnt_red.setStrokeWidth(50f);
            pnt_red.setColor(Color.rgb(255, 0, 0));
            pnt_red.setStyle(Paint.Style.STROKE);

            canvas.drawArc(rect, 140, sweepAngle, false, pnt_red);

        } else {
            if (sweepAngle < maxAngle) {
                Paint pnt_green = new Paint();
                pnt_green.setStrokeWidth(50f);
                pnt_green.setColor(Color.rgb(146, 208, 80));
                pnt_green.setStyle(Paint.Style.STROKE);

                canvas.drawArc(rect, 140, sweepAngle, false, pnt_green);
            } else {
                Paint pnt_orange = new Paint();
                pnt_orange.setStrokeWidth(50f);
                pnt_orange.setColor(Color.rgb(255, 192, 0));
                pnt_orange.setStyle(Paint.Style.STROKE);

                canvas.drawArc(rect, 140, sweepAngle, false, pnt_orange);
            }
        }
    }
}
