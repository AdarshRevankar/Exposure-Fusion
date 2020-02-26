package com.adrino.renderscript;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Type;

import java.util.ArrayList;
import java.util.List;

public class RsUtils {
    public static Allocation create2d(RenderScript rs, int width, int height, Element elementType) {
        Type.Builder vectorBufferBuilder = new Type.Builder(rs, elementType);
        vectorBufferBuilder.setX(width);
        vectorBufferBuilder.setY(height);
        return Allocation.createTyped(rs, vectorBufferBuilder.create(), Allocation.USAGE_SCRIPT);
    }
    public static void ErrorViewer(Object where, String title, String subjet){
        Log.e("EV", " ");
        Log.e("EV", " ");
        Log.e("EV", " + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
        Log.e("EV", " |  "+where.getClass()+" : "+title );
        Log.e("EV", " + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
        Log.e("EV", " |  "+ subjet);
        Log.e("EV", " + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
        Log.e("EV", " ");
        Log.e("EV", " ");
    }
    public static void ErrorViewer(Object where, String title, String subject, String remarks){
        Log.e("EV", " ");
        Log.e("EV", " ");
        Log.e("EV", " + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
        Log.e("EV", " |  "+where.getClass()+": "+title );
        Log.e("EV", " + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
        Log.e("EV", " |  "+ subject);
        Log.e("EV", " + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
        Log.e("EV", " |  "+ remarks);
        Log.e("EV", " + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
        Log.e("EV", " ");
        Log.e("EV", " ");

    }

    public static Bitmap resizeBmp(Bitmap inBmpImage){
        int imgWidth = inBmpImage.getWidth();
        int imgHeight = inBmpImage.getHeight();

        int scaledWidth = imgHeight > imgWidth ? (imgWidth * Constant.SCALE_THRUSHOLD) / imgHeight : Constant.SCALE_THRUSHOLD;
        int scaledHeight = imgHeight > imgWidth ? Constant.SCALE_THRUSHOLD : (imgHeight * Constant.SCALE_THRUSHOLD) / imgWidth;
        return Bitmap.createScaledBitmap(inBmpImage, scaledWidth, scaledHeight, false);
    }

    public static List<Bitmap> resizeBmp(List<Bitmap> inBmpImageList){
        List<Bitmap> outBmpImageList = new ArrayList<>(inBmpImageList.size());
        for (Bitmap inBmpImage: inBmpImageList) {
            outBmpImageList.add(
                    resizeBmp(inBmpImage)
            );
        }
        return outBmpImageList;
    }
}

class Level{
    int width, height;

    Level(int width, int height){
        this.width = width;
        this.height = height;
    }
}
