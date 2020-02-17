package com.adrino.renderscript;

import android.util.Log;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Type;

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
}
