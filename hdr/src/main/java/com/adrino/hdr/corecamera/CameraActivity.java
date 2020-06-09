package com.adrino.hdr.corecamera;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.adrino.hdr.R;
import com.adrino.hdr.corecamera.utils.Constants;

public class CameraActivity extends AppCompatActivity{

    private static final String TAG = "CameraActivity";
    private CameraCapture cameraCapture;
    private Constants.CameraLens cameraLens = Constants.CameraLens.LENS_FACING_BACK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(getThemeMode());
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);   // Initial Animation
        setContentView(R.layout.activity_camera);

        // If first time activity is loaded
        if (savedInstanceState == null) {
            cameraCapture = CameraCapture.newInstance(cameraLens);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, cameraCapture)
                    .commit();
        }
    }

    /**
     * =============================================================================================
     * OnClick - Listeners
     * =============================================================================================
     */
    public void changeEV(View view) {
        if (view.getId() == R.id.exposurePlus) {
            // If + button Clicked, then Low = Low - Inc and High = High + Inc
            if (Constants.getLowEV() - Constants.getIncrement() < Constants.getHighEV() + Constants.getIncrement()) {
                Constants.setLowEV(Constants.getLowEV() - Constants.getIncrement());
                Constants.setHighEV(Constants.getHighEV() + Constants.getIncrement());
            }
        } else if (view.getId() == R.id.exposureMinus) {
            // If - button Clicked, then Low = Low + Inc and High = High - Inc
            if (Constants.getLowEV() + Constants.getIncrement() < Constants.getHighEV() - Constants.getIncrement()) {
                Constants.setLowEV(Constants.getLowEV() + Constants.getIncrement());
                Constants.setHighEV(Constants.getHighEV() - Constants.getIncrement());
            }
        }
        updateUI();
    }

    public void changeCamera(View view){
        if (view.getId() == R.id.changeCamera) {
            cameraCapture.onDestroy();
            cameraLens = cameraLens == Constants.CameraLens.LENS_FACING_BACK ?
                    Constants.CameraLens.LENS_FACING_FRONT :
                    Constants.CameraLens.LENS_FACING_BACK;
            cameraCapture = CameraCapture.newInstance(cameraLens);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, cameraCapture)
                    .commit();
        }
    }

    /**
     * =============================================================================================
     * Helper Methods
     * =============================================================================================
     */
    private void updateUI() {
        ((TextView)findViewById(R.id.tvLow)).setText("Low "+Constants.getLowEV());
        ((TextView)findViewById(R.id.tvMid)).setText("Mid "+Constants.getMidEV());
        ((TextView)findViewById(R.id.tvHigh)).setText("High "+Constants.getHighEV());
    }

    private int getThemeMode() {
        boolean darkModeOn = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
        return darkModeOn ? R.style.DarkTheme : R.style.AppTheme;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraLens = null;
        cameraCapture.onDestroy();
        cameraCapture = null;
    }
}
