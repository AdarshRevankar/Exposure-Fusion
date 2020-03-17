package com.adrino.hdr.camera;

import android.app.Activity;
import android.graphics.Bitmap;

import java.util.List;

public interface CameraManager {
    interface CameraController {

    }

    interface CameraPresenter {
        void startCameraActivity(Activity activity);
        List<Bitmap> onCapturingSuccess(List<Bitmap> imageList);
    }
}
