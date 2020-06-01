package com.adrino.hdr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.adrino.hdr.corecamera.CameraActivity;
import com.adrino.hdr.corehdr.*;
import com.adrino.hdr.corehdr.CreateHDR;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Manager is the Binder which does the job of combining the Camera View & HDR Manager
 * +----------------------------------------------------+
 * | Requires                                           |
 * | 1. {@link com.adrino.hdr.corehdr.CreateHDR};       |
 * | 2. {@link com.adrino.hdr.corecamera.CameraActivity}|
 * | 3. {@link com.adrino.hdr.corehdr.Constants};       |
 * +----------------------------------------------------+
 */
public class Manager {

    private CreateHDR createHDR = null;
    private Context context = null;

    public Manager(Context context) {
        this.context = context;
        createHDR = new CreateHDR(this.context);
    }

    /**
     *  ==================================================================
     *  Interfacing Function - Perform
     *  1. perform(bmpList, Action) - Performs Action over bmpList
     *  2. perform(activity) - Inflates the CameraView Activity
     *  2. perform(activity, boolean) - 1 + 2 Combined ie. Capture + HDR
     *  ==================================================================
     */
    public List<Bitmap> perform(List<Bitmap> bmpInputImage, CreateHDR.Actions action) {
        // PERFORM ACTION
        return new ArrayList<>(createHDR.perform(bmpInputImage, action));
    }

    public void perform(Activity currActivity) {
        // PERFORM INFLATION OF VIEW
        createCameraView(currActivity);
    }

    public List<Bitmap> perform(Activity currActivity, boolean deleteImages) {
        // PERFORM, INFLATION + HDR
        perform(currActivity);

        // Perform HDR and return the HDR
        File parentFile = currActivity.getExternalFilesDir(null);
        return perform(loadImages(parentFile, deleteImages), CreateHDR.Actions.HDR);
    }


    /**
     * ==================================================================
     * Helper Functions
     * ==================================================================
     */
    private List<Bitmap> loadImages(File file, boolean deleteImages) {
        List<Bitmap> bmpImageList = new ArrayList<>(Constants.INPUT_IMAGE_SIZE);

        for (int imgIndex = 1; imgIndex <= Constants.INPUT_IMAGE_SIZE; imgIndex++) {

            // Load Images
            File imgFile = new File(file, "pic" + imgIndex + ".jpg");
            bmpImageList.add(BitmapFactory.decodeFile(imgFile.getAbsolutePath()));

            // Delete Images if required
            if (deleteImages && imgFile.delete()) {
                Log.e(TAG, "loadImages: Image Deleted " + imgIndex);
            }
        }
        return bmpImageList;
    }

    private void createCameraView(Activity activity) {
        Intent switchActivity = new Intent(context, CameraActivity.class);
        activity.startActivity(switchActivity);
    }

    // Getter
    public List<Bitmap> getBmpImageList(File file) {
        return loadImages(file, false);
    }
}
