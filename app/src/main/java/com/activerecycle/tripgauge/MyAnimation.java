package com.activerecycle.tripgauge;

import android.animation.ValueAnimator;
import android.view.View;

// 페이드 아웃/페이드 인 애니메이션을 적용하기 위한 클래스
public class MyAnimation {

    public static void fadeIn(View view, long duration) {
        ValueAnimator animator = ValueAnimator.ofFloat(0.3f, 1f);  // 완전히 사라지는 것을 원한다면 0.3f->0f로 수정 바람
        animator.setDuration(duration);
        animator.addUpdateListener(animation -> {
            float alpha = (float) animation.getAnimatedValue();
            view.setAlpha(alpha);
        });
        animator.start();
    }

    public static void fadeOut(View view, long duration) {
        ValueAnimator animator = ValueAnimator.ofFloat(1f, 0.3f);  // 완전히 사라지는 것을 원한다면 0.3f->0f로 수정 바람
        animator.setDuration(duration);
        animator.addUpdateListener(animation -> {
            float alpha = (float) animation.getAnimatedValue();
            view.setAlpha(alpha);
        });
        animator.start();
    }
}
