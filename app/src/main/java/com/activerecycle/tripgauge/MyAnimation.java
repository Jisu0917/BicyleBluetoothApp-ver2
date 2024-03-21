package com.activerecycle.tripgauge;

import android.animation.ValueAnimator;
import android.view.View;

public class MyAnimation {

    public static void fadeIn(View view, long duration) {
        ValueAnimator animator = ValueAnimator.ofFloat(0.3f, 1f);
        animator.setDuration(duration);
        animator.addUpdateListener(animation -> {
            float alpha = (float) animation.getAnimatedValue();
            view.setAlpha(alpha);
        });
        animator.start();
    }

    public static void fadeOut(View view, long duration) {
        ValueAnimator animator = ValueAnimator.ofFloat(1f, 0.3f);
        animator.setDuration(duration);
        animator.addUpdateListener(animation -> {
            float alpha = (float) animation.getAnimatedValue();
            view.setAlpha(alpha);
        });
        animator.start();
    }
}
