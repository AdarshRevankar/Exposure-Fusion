package com.adrino.hdr.camera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;

import java.util.List;

public class CameraViewer implements CameraManager.CameraPresenter{

    public static final int REQUEST_CODE = 10009;

    @Override
    public void startCameraActivity(Activity activity) {
        Intent switchActivity = new Intent(activity.getApplicationContext(), CameraActivity.class);
        activity.startActivityForResult(switchActivity, REQUEST_CODE);
    }

    @Override
    public List<Bitmap> onCapturingSuccess(List<Bitmap> acquiredImageList) {
        return acquiredImageList;
    }
}
