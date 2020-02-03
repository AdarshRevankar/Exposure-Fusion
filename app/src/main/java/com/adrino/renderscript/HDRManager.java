package com.adrino.renderscript;

import android.graphics.Bitmap;

public interface HDRManager {
    interface Presenter{
        Bitmap applyGrayScaleFilter(Bitmap bmpImage);
        Bitmap applyConvolution3x3Filter(Bitmap bmpImage);
        Bitmap applySaturationFilter(Bitmap bmpImage);
        Bitmap applyExposureFilter(Bitmap bmpImage);
        Bitmap[] computeNormalWeighted(Bitmap[] bmpImages);
        void destoryRenderScript();
    }

    interface Viewer{

    }
}
