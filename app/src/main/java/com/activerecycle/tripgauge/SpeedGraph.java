package com.activerecycle.tripgauge;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;

// Consumption 페이지에 쓰이는 속도 원호 그래프를 그리기 위한 뷰
public class SpeedGraph extends View {
    public SpeedGraph(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    final int MAX_SPEED = 25;
    public static int speed;
    int sweepAngle;
    int maxAngle;
    private ValueAnimator animator;
    private float sweepingAngle = 0;
    //boolean btconnected = false;

    public static int previousSpeed = 0;

    @Override
    protected void onDraw(android.graphics.Canvas canvas) {
        super.onDraw(canvas);

        if (speed == 99) {  // 블루투스 미연결시 깜빡이는 애니매이션을 위한 설정
            // 블루투스 연결 안 된 상태를 나타냄
            sweepAngle = 3;
            //btconnected = false;
        } else {
            //btconnected = true;
            sweepAngle = getSweepAngle(speed);
        }
        maxAngle = getSweepAngle(MAX_SPEED);


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
        rect.set(screenWidth/2 - 500, 30, screenWidth/2 + 500, 980);
        canvas.drawArc(rect, 140, 260, false, pnt_gray);

        //블루투스 연결 안 된 상태일 때
        if (!ConsumptionActivity.btconnect) {
            cancelAnimation();  //남아있는 직전 애니매이션을 지운다.
            if (speed == 0) {
                previousSpeed = 99;
                animator = ValueAnimator.ofFloat(0, 3); // 시작 각도와 종료 각도 설정
                sweepAngle = 0;
            } else {
                previousSpeed = 0;
                animator = ValueAnimator.ofFloat(0, 3); // 시작 각도와 종료 각도 설정
                sweepAngle = 3;
            }

            Paint pnt_red = new Paint();
            pnt_red.setStrokeWidth(50f);
            pnt_red.setColor(Color.rgb(255, 0, 0));
            pnt_red.setStyle(Paint.Style.STROKE);

            canvas.drawArc(rect, 140, sweepAngle, false, pnt_red);

        } else {//블루투스 연결된 상태일 때
            if (speed <= 30) {
                animator = ValueAnimator.ofFloat(getSweepAngle(previousSpeed), sweepAngle); // 시작 각도와 종료 각도 설정
            } else {  // 속력이 30을 초과하면 각도는 30일 때의 각도로 한다.(넘어가지 않게 설정)
                animator = ValueAnimator.ofFloat(getSweepAngle(previousSpeed), getSweepAngle(30)); // 시작 각도와 종료 각도 설정
            }
            animator.setDuration(5000); // 애니메이션 지속 시간 설정
            animator.setInterpolator(new AccelerateDecelerateInterpolator()); // 가속도와 감속도를 조절하는 인터폴레이터 설정
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    sweepingAngle = (float) animation.getAnimatedValue();
                    invalidate(); // 그래픽을 다시 그리도록 요청
                }
            });


            if (sweepAngle < maxAngle) {  // 설정 최대치를 안 넘으면 연두색으로 칠한다.
                Paint pnt_green = new Paint();
                pnt_green.setStrokeWidth(50f);
                pnt_green.setColor(Color.rgb(146, 208, 80));
                pnt_green.setStyle(Paint.Style.STROKE);

                canvas.drawArc(rect, 140, sweepingAngle, false, pnt_green);

                previousSpeed = speed;

            } else { // 설정 최대치를 넘으면 오렌지 색으로 칠한다.
                Paint pnt_orange = new Paint();
                pnt_orange.setStrokeWidth(50f);
                pnt_orange.setColor(Color.rgb(255, 192, 0));
                pnt_orange.setStyle(Paint.Style.STROKE);

                canvas.drawArc(rect, 140, sweepingAngle, false, pnt_orange);

                previousSpeed = speed;
            }
        }
    }

    // 애니매이션을 시작하는 함수
    public void startAnimation() {
        try {
            animator.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 애니매이션을 취소하는 함수
    public void cancelAnimation() {
        try {
            animator.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 속도 값에 따른 회전 각도를 계산해서 반환하는 함수
    private int getSweepAngle(int speed) {

        if (speed <= 30) {
            return 260 * speed / 30;
        } else {
            return 260;
        }
    }

    // 회전 각도에 따른 속도를 역으로 계산해서 반환하는 함수
    private int getSpeedByAngle(int angle) {
        return angle * 30 / 260;
    }
}
