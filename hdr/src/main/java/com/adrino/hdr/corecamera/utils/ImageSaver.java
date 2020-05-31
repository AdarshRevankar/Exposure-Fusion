package com.adrino.hdr.corecamera.utils;

import android.app.Activity;
import android.media.Image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ImageSaver implements Runnable {
    private Image mImage;
    private File mFile;
    public static int writtenCount = 1;
    private static Activity activity;

    public ImageSaver(Activity fragmentActivity, Image image, File file) {
        if(writtenCount == 1)
            activity = fragmentActivity;
        writtenCount++;
        mImage = image;
        mFile = file;
    }

    @Override
    public void run() {
        ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(mFile);
            output.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mImage.close();
            if (null != output) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            clear();
        }
        if (writtenCount > 3) {
            activity.finish();
            writtenCount = 0;
        }
    }

    void clear() {
        mImage.close();
        mImage = null;
        mFile = null;
    }
}
