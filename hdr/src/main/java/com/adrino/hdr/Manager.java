package com.adrino.hdr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.adrino.hdr.corecamera.CameraActivity;
import com.adrino.hdr.corehdr.Constants;
import com.adrino.hdr.corehdr.CreateHDR;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class Manager {

    private CreateHDR createHDR = null;
    private Context context = null;
    List<Bitmap> bmpImageList = null;

    public Manager(Context context) {
        this.context = context;
        createHDR = new CreateHDR(this.context);
    }

    public List<Bitmap> perform(List<Bitmap> bmpInputImage, CreateHDR.Actions action) {
        return new ArrayList<>(createHDR.perform(bmpInputImage, action));
    }

    public List<Bitmap> perform(Activity currActivity, boolean saveMultiExposureImages) {
        // Start the activity
        createCameraView(currActivity);

        // Perform HDR and return the HDR
        File parentFile = currActivity.getExternalFilesDir(null);
        return perform(loadImages(parentFile, saveMultiExposureImages), CreateHDR.Actions.HDR);
    }

    private List<Bitmap> loadImages(File file, boolean saveMultiExposureImages) {
        bmpImageList = new ArrayList<>(Constants.INPUT_IMAGE_SIZE);

        for (int imgIndex = 1; imgIndex <= Constants.INPUT_IMAGE_SIZE; imgIndex++) {

            // Load Images
            File imgFile = new File(file, "pic" + imgIndex + ".jpg");
            bmpImageList.add(BitmapFactory.decodeFile(imgFile.getAbsolutePath()));

            // Delete Images if required
            if ( !saveMultiExposureImages && imgFile.delete()){
                Log.e(TAG, "loadImages: Image Deleted "+imgIndex);
            }
        }
        return new ArrayList<>(bmpImageList);
    }

    private void createCameraView(Activity activity) {
        Intent switchActivity = new Intent(context, CameraActivity.class);
        activity.startActivity(switchActivity);
    }

    public List<Bitmap> getBmpImageList() {
        return new ArrayList<>(bmpImageList);
    }
}
