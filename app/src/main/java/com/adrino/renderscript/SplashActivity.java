package com.adrino.renderscript;

import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.adrino.renderscript.utils.AnimationHandlers;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "S[;ash";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                setTheme(R.style.DarkTheme);
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                setTheme(R.style.AppTheme);
                break;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Get Progressbar change its color
        ImageView logoView = findViewById(R.id.logoIco);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar_cyclic);
        progressBar.getIndeterminateDrawable().setColorFilter(0xFF4DABF5, PorterDuff.Mode.MULTIPLY);

        // For the Logo Animation
        AnimationHandlers.fadeAnimator(this, logoView, 0, 400, 400, 300);
        AnimationHandlers.delayedIntent(this, MainActivity.class, 1700);
    }
}