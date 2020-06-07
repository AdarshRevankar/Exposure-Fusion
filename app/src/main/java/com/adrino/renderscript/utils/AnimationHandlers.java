package com.adrino.renderscript.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;

public class AnimationHandlers {
    private static int progressStatus = 0;

    public static void delayedIntent(final Activity activity, final Class<?> toClass, int delay) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // This method will be executed once the timer is over
                Intent i = new Intent(activity, toClass);
                activity.startActivity(i);
                activity.finish();
            }
        }, delay);
    }

    public static void startProgressBar(final ProgressBar progressBar, int increment, final Handler handler) {
        // Error Handler Increment Value
        final int incrementValue = 0 < increment && increment <= 100 ? increment : 1;

        // Start Update thread
        new Thread(new Runnable() {
            public void run() {
                while (progressStatus < 100) {
                    progressStatus += incrementValue;
                    handler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(progressStatus);
                        }
                    });
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public static void fadeAnimator(
            final Activity activity,
            final View element,
            final int fadeInStart,
            final int fadeInDuration,
            final int fadeOutStart,
            final int fadeOutDuration) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Animation fadeIn = new AlphaAnimation(0, 1);
                fadeIn.setInterpolator(new DecelerateInterpolator());
                fadeIn.setStartOffset(fadeInStart);
                fadeIn.setDuration(fadeInDuration);

                Animation fadeOut = new AlphaAnimation(1, 0);
                fadeOut.setInterpolator(new AccelerateInterpolator());
                fadeOut.setStartOffset(fadeOutStart);
                fadeOut.setDuration(fadeOutDuration);

                final AnimationSet animation = new AnimationSet(false);
                animation.addAnimation(fadeIn);
                animation.addAnimation(fadeOut);

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        element.startAnimation(animation);
                    }
                });
            }
        }).start();
    }
}
