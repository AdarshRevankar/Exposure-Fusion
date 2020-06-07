package com.adrino.renderscript;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.adrino.renderscript.utils.AnimationHandlers;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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