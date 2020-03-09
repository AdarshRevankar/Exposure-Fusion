package com.adrino.renderscript;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.adrino.hdr.camera.CameraCapture;

public class CameraActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, CameraCapture.newInstance(MainActivity.class))
                    .commit();
        }
    }
}
