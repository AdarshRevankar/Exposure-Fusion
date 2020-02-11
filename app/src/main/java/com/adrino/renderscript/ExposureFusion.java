package com.adrino.renderscript;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.renderscript.Allocation;

import java.util.List;

public class ExposureFusion implements HDRManager.Presenter {

    private static final String TAG = "ExposureFusion";
    private Context mainActivityContext;
    private HDRFilter hdrFilter;
    enum Actions { CONTRAST, SATURATION, EXPOSED, NORMAL};
    private static List<Allocation> contrast;
    private static List<Allocation> saturation;
    private static List<Allocation> well_exposedness;
    private static List<Allocation> normal;

    ExposureFusion(Context context){
        this.mainActivityContext = context;
        hdrFilter = new HDRFilter(context);
    }

    @Override
    public void setMeta(int imgWidth, int imgHeight, Bitmap.Config imgConfig) {
        hdrFilter.setMeta(imgWidth, imgHeight, imgConfig);
    }

    @Override
    public List<Bitmap> perform(List<Bitmap> bmpImagesList, Actions action) {

        if(contrast == null) contrast = hdrFilter.applyConvolution3x3Filter(bmpImagesList);
        if(saturation == null) saturation = hdrFilter.applySaturationFilter(bmpImagesList);
        if(well_exposedness == null) well_exposedness = hdrFilter.applyExposureFilter(bmpImagesList);
        if(normal == null) normal = hdrFilter.computeNormalWeighted(contrast, saturation, well_exposedness);

        switch (action) {
            case CONTRAST:
                return HDRFilter.convertAllocationToBMP(contrast);
            case SATURATION:
                return HDRFilter.convertAllocationToBMP(saturation);
            case EXPOSED:
                return HDRFilter.convertAllocationToBMP(well_exposedness);
            case NORMAL:
                return HDRFilter.convertAllocationToBMP(normal);
        }
        return null;
    }
}
