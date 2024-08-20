package com.example.myproject.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.LinearInterpolator;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.google.android.material.button.MaterialButton;

public class AnimationUtils {

    public static void fadeIn(View view, long duration) {
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(duration);
        fadeIn.setInterpolator(new LinearInterpolator());
        view.startAnimation(fadeIn);
        view.setVisibility(View.VISIBLE);
    }

    public static void addShineEffectToButton(MaterialButton button, Drawable shineDrawable) {
        Context context = button.getContext();
        View shineOverlay = new View(context);
        shineOverlay.setId(View.generateViewId());
        shineOverlay.setBackground(shineDrawable);

        ConstraintLayout layout = (ConstraintLayout) button.getParent();
        layout.addView(shineOverlay, new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
                ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
        ));

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(layout);
        constraintSet.constrainWidth(shineOverlay.getId(), button.getWidth());
        constraintSet.constrainHeight(shineOverlay.getId(), button.getHeight());
        constraintSet.connect(shineOverlay.getId(), ConstraintSet.TOP, button.getId(), ConstraintSet.TOP);
        constraintSet.connect(shineOverlay.getId(), ConstraintSet.BOTTOM, button.getId(), ConstraintSet.BOTTOM);
        constraintSet.connect(shineOverlay.getId(), ConstraintSet.START, button.getId(), ConstraintSet.START);
        constraintSet.connect(shineOverlay.getId(), ConstraintSet.END, button.getId(), ConstraintSet.END);
        constraintSet.applyTo(layout);

        ValueAnimator animator = ValueAnimator.ofFloat(-1f, 1f);
        animator.setDuration(2000);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            shineOverlay.setTranslationX(value * button.getWidth());
        });
        animator.start();
    }

    public static AnimatorSet startShiningEffect(View view) {
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f);
        scaleXAnimator.setDuration(1000);
        scaleXAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleXAnimator.setRepeatMode(ValueAnimator.REVERSE);
        scaleXAnimator.setRepeatCount(ValueAnimator.INFINITE);

        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 1f);
        scaleYAnimator.setDuration(1000);
        scaleYAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleYAnimator.setRepeatMode(ValueAnimator.REVERSE);
        scaleYAnimator.setRepeatCount(ValueAnimator.INFINITE);

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.8f, 1f);
        alphaAnimator.setDuration(1000);
        alphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        alphaAnimator.setRepeatMode(ValueAnimator.REVERSE);
        alphaAnimator.setRepeatCount(ValueAnimator.INFINITE);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleXAnimator, scaleYAnimator, alphaAnimator);
        animatorSet.start();

        return animatorSet;
    }

    public static void stopShiningEffect(AnimatorSet animatorSet) {
        if (animatorSet != null && animatorSet.isRunning()) {
            animatorSet.end();
        }
    }
}
