package com.adrino.renderscript;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class CameraActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, CameraCapture.newInstance())
                    .commit();
        }
    }
}
