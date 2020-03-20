package com.adrino.hdr.corecamera.utils;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.adrino.hdr.corecamera.CameraUtils;

import static android.content.Context.SENSOR_SERVICE;

public class WobbleCheck implements SensorEventListener {
    private static final String FRAGMENT_WOBBLE = "check";
    private static final String FRAGMENT_SUCCESS = "SUCCESS";
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float mAccel;
    private double mAccelCurrent;
    private double mAccelLast;
    private boolean isImageCaptureStart = false;
    private boolean isImageCaptureEnd = false;
    private boolean isMobilePositionChanged = false;
    private static final float MAX_ACCELERATION_ALLOWED = 0.3f;

    // Constructor
    public WobbleCheck(@NonNull FragmentActivity fragmentActivity) {
        sensorManager = (SensorManager) fragmentActivity.getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
        setImageCaptureStart(false);
        setImageCaptureEnd(false);
        setMobilePositionChanged(false);
    }

    // Getter & Setter
    private synchronized boolean isImageCaptureStart() {
        return isImageCaptureStart;
    }

    public synchronized void setImageCaptureStart(boolean imageCaptureStart) {
        isImageCaptureStart = imageCaptureStart;
    }

    private synchronized boolean isImageCaptureEnd() {
        return isImageCaptureEnd;
    }

    public synchronized void setImageCaptureEnd(boolean imageCaptureEnd) {
        isImageCaptureEnd = imageCaptureEnd;
    }

    private synchronized boolean isMobilePositionChanged() {
        return isMobilePositionChanged;
    }

    private synchronized void setMobilePositionChanged(boolean mobilePositionChanged) {
        isMobilePositionChanged = mobilePositionChanged;
    }

    public void check(FragmentManager fragmentManager) {
        if (isMobilePositionChanged()) {
            new CameraUtils.WobbleDialog().show(fragmentManager, FRAGMENT_WOBBLE);
            CameraUtils.WobbleDialog.newInstance("Please dont move your device")
                    .show(fragmentManager, FRAGMENT_WOBBLE);
        } else {
            CameraUtils.SuccessDialog.newInstance("Image Captured successfully")
                    .show(fragmentManager, FRAGMENT_SUCCESS);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Shake detection
            float[] mGravity = event.values.clone();
            float x = mGravity[0];
            float y = mGravity[1];
            float z = mGravity[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = Math.sqrt(x * x + y * y + z * z);
            double delta = mAccelCurrent - mAccelLast;
            mAccel = (float) (mAccel * 0.9f + delta);
            if (mAccel > MAX_ACCELERATION_ALLOWED) {
                setMobilePositionChanged(isImageCaptureStart() && !isImageCaptureEnd());
                Log.e("Mobile Position ", "Mobile Accerated");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void registerListener() {
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void unRegisterListener() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }
}
