package com.activerecycle.tripgauge;

import static android.graphics.Path.FillType.EVEN_ODD;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Map;

// TripLog 그래프를 그리기 위한 뷰
public class LogGraph extends View {

    static Map map;
    static int n = 0, m = 0;
    static float max, min;
    static int maxW, usedW, avrW, sumW;
    final static int DEFINED_MAX_W = 750;
    int top, bottom, adjustY;
    int TOP, BOTTOM;

    float adjustC;

    public LogGraph(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.BLACK);

        map = TripLogActivity.dataMap;

        // 데이터가 비어있으면 그래프를 그릴 수 없다.
        if (map == null || map.size() == 0 || map.get("W") == null) {
            System.out.println("!!! map size is 0.");
            return;
        }

        ArrayList<Integer> original_wList = (ArrayList<Integer>) map.get("W");
        n = original_wList.size();

        // 데이터가 비어있으면 그래프를 그릴 수 없다.
        if (n == 0) {
            return;
        }
        // usedW, avrpwr, maxW 구하기
        usedW = original_wList.get(0) - original_wList.get(n-1);
        if (usedW < 0) { usedW = -usedW; }
        maxW = original_wList.get(0);
        sumW = 0;
        for (int i=1; i < n; i++) {
            if (original_wList.get(i) > maxW) maxW = original_wList.get(i);;
            sumW += original_wList.get(i);
        }
        avrW = sumW / n;

        // 그래프를 절댓값이 아닌 상댓값으로 하여 그래프 상자 크기에 맞게 값을 조정한다.
        ArrayList<Float> value = new ArrayList<>();
        float fitC = 0.5f;
        for (int i = 0; i < n; i++) {
            int k = original_wList.get(i);
            value.add(k * fitC);
        }

        // 최대값, 최소값 구하기
        max = value.get(0);
        min = value.get(0);
        for (int i=1; i < n; i++) {
            if (value.get(i) > max) max = value.get(i);
            if (value.get(i) < min) min = value.get(i);
        }

        top = 150;
        bottom = getHeight() -550;

        // 흰색 사각형 틀 기준 탑, 바텀
        TOP = 100;
        BOTTOM = getHeight() - 40;

        final int margin = 150;
        m = n;
        final int dotDistance = (getWidth() - 2*margin) / m;  // 간격 개수 m - 1, 시작끝 좌우여백
        //final int dotDistance = 10;
        final int firstDotX = 80 + margin + dotDistance/2;


        adjustC = 0.6f * DEFINED_MAX_W;  // C로 나누기 때문에 C가 커질수록 그래프가 높아진다.
        adjustY = 330;


        Paint dotPaint = new Paint();
        dotPaint.setColor(Color.WHITE);
        dotPaint.setStrokeWidth(0);

        Paint pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setStyle(Paint.Style.FILL);
        pathPaint.setAntiAlias(true);
        pathPaint.setStrokeJoin(Paint.Join.ROUND);
        pathPaint.setStrokeCap(Paint.Cap.ROUND);
        pathPaint.setDither(true);

        float y0;
        if (maxW > 500) { y0 = getHeight()*0.57f; }  //400
        else if (maxW > 300) { y0 = getHeight()*0.64f; }  //450
        else if (maxW > 100) { y0 = getHeight()*0.71f; }  //500
        else { y0 = getHeight()*0.78f; }  //550
        LinearGradient linearGradient = new LinearGradient(200, y0, 200, getHeight()*0.85f, Color.rgb(235, 0, 0), Color.BLACK, Shader.TileMode.CLAMP);
        pathPaint.setShader(linearGradient);


        Paint pathStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathStrokePaint.setStyle(Paint.Style.STROKE);
        pathStrokePaint.setColor(Color.WHITE);
        pathStrokePaint.setStrokeWidth(5f);
        pathStrokePaint.setAntiAlias(true);
        pathStrokePaint.setStrokeJoin(Paint.Join.ROUND);
        pathStrokePaint.setStrokeCap(Paint.Cap.ROUND);
        pathStrokePaint.setDither(true);

        Path p = new Path();
        p.setFillType(EVEN_ODD);

        //p.moveTo(210, getHeight() - 40);
        //p.lineTo(firstDotX, getPointY(0));
        p.moveTo(firstDotX, getPointY(0));
        for (int i = 0; i < n; i++) {
            // 꼭짓점 그리기
            //canvas.drawCircle(firstDotX + dotDistance * i, bottom - (value.get(i) * max / adjustC) + adjustY, 5, dotPaint);

            // "이전 꼭짓점"과 연결해주는 선 그리기
            if (i > 0) {
                p.lineTo(firstDotX + dotDistance * (i-1), getPointY(value.get(i-1)));
            }

        }
        p.lineTo(firstDotX + dotDistance * (n-1), getPointY(value.get(n-1)));
        p.lineTo((getWidth() - 100), (getHeight() - 40));
        //p.close();
        canvas.drawPath(p, pathPaint);
        canvas.drawPath(p, pathStrokePaint);

        p.setFillType(Path.FillType.INVERSE_WINDING);


        // 흰색 사각형 틀 (X, Y축을 대신한다.)
        Paint paint = new Paint(); // 페인트 객체 생성
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f);
        canvas.drawRoundRect(200, TOP, getWidth() - 60, BOTTOM, 25f, 25f, paint);

        Typeface typeface = Typeface.createFromAsset(getResources().getAssets(), "gmarket_sans_bold.ttf");

        // 0 그리기
        Paint txtPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        txtPaint.setTypeface(typeface);
        txtPaint.setColor(Color.WHITE);
        txtPaint.setTextSize(46f);
        txtPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("0", 150, getHeight() - 50, txtPaint);

        // 최댓값 숫자
        canvas.drawText(maxW + "", 150, getPointY(max), txtPaint);

        // 최댓값 점선
        Paint linePaint = new Paint();
        DashPathEffect dashPath = new DashPathEffect(new float[]{20,20}, 2);
        linePaint.setStyle( Paint.Style.STROKE );
        linePaint.setPathEffect(dashPath);
        linePaint.setStrokeWidth(4);
        linePaint.setColor(Color.WHITE);

        Path path = new Path();
        for (int i = 0; i < n; i++) {
            if (value.get(i) == max) {
                path.moveTo(200, getPointY(max));
                path.lineTo(firstDotX + dotDistance * i, getPointY(max));
            }
        }
        canvas.drawPath(path, linePaint);

        // Consumption Log 글씨 넣기
        txtPaint.setTextSize(44f);

        canvas.drawText("CONSUMPTION LOG", getWidth() - 80, 160, txtPaint);
    }

    // Y 포인트 지점 값을 반환하는 함수
    private float getPointY(float y) {
        adjustY = 0;
        adjustC = 0.8f;

        if (y == 0) return BOTTOM;
        return BOTTOM - ( ( BOTTOM - TOP ) * (y / max) * adjustC);  //adjustC 값이 커질수록 그래프가 커진다.
    }
}
