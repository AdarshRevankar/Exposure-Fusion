package com.adrino.hdr.camera;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.adrino.hdr.R;
import com.adrino.hdr.camera.corecamera.Constants;

public class CameraActivity extends AppCompatActivity implements
        CameraManager.CameraController,
        View.OnClickListener {

    private static final String TAG = "CameraActivty";
    private CameraCapture cameraCapture;
    private Constants.CameraLens cameraLens;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // If first time activity is loaded
        if (savedInstanceState == null) {
            cameraCapture = CameraCapture.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, cameraCapture)
                    .commit();
        }

        // Set on click listener
        findViewById(R.id.changeCamera).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.changeCamera) {
            Log.e(TAG, "onClick: Changing camera" );
            cameraCapture.onDestroy();
            cameraLens = cameraLens == Constants.CameraLens.LENS_FACING_BACK ?
                    Constants.CameraLens.LENS_FACING_FRONT :
                    Constants.CameraLens.LENS_FACING_BACK;
            cameraCapture = CameraCapture.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, cameraCapture)
                    .commit();
        }
    }

    @Override
    protected void onDestroy() {
        cameraLens = null;
        cameraCapture.onDestroy();
        cameraCapture = null;
        super.onDestroy();
    }
}
