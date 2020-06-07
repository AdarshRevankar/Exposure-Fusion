package com.adrino.hdr.corecamera;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.adrino.hdr.R;
import com.adrino.hdr.corecamera.utils.Constants;

public class CameraActivity extends AppCompatActivity implements
        View.OnClickListener {

    private static final String TAG = "CameraActivity";
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
            Log.e(TAG, "onClick: Changing camera");
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

    public void changeEV(View view) {
        if (view.getId() == R.id.exposurePlus) {
            if (Constants.getLowEV() - Constants.getIncrement() < Constants.getHighEV() + Constants.getIncrement()) {
                Constants.setLowEV(Constants.getLowEV() - Constants.getIncrement());
                Constants.setHighEV(Constants.getHighEV() + Constants.getIncrement());
            }
        } else if (view.getId() == R.id.exposureMinus) {
            if (Constants.getLowEV() + Constants.getIncrement() < Constants.getHighEV() - Constants.getIncrement()) {
                Constants.setLowEV(Constants.getLowEV() + Constants.getIncrement());
                Constants.setHighEV(Constants.getHighEV() - Constants.getIncrement());
            }
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
