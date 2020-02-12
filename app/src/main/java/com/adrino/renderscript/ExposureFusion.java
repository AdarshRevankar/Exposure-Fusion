package com.adrino.renderscript;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.renderscript.Allocation;

import java.util.List;

public class ExposureFusion implements HDRManager.Presenter {

    private static final String TAG = "ExposureFusion";
    private static int SELECTED_INDEX = 0;
    private HDRFilter hdrFilter;
    private static List<Allocation> resultant;
    private List<Allocation> collapse;

    enum Actions {CONTRAST, SATURATION, EXPOSED, NORMAL, GAUSSIAN, LAPLACIAN, RESULTANT, COLLAPSE}

    ;

    private static List<Allocation> contrast;
    private static List<Allocation> saturation;
    private static List<Allocation> well_exposedness;
    private static List<Allocation> normal;
    private static List<List<Allocation>> gaussian;
    private static List<List<Allocation>> laplacian;

    ExposureFusion(Context context) {
        hdrFilter = new HDRFilter(context);
    }

    @Override
    public void setMeta(int imgWidth, int imgHeight, Bitmap.Config imgConfig) {
        hdrFilter.setMeta(imgWidth, imgHeight, imgConfig);
    }

    @Override
    public List<Bitmap> perform(List<Bitmap> bmpImagesList, Actions action) {

        if (contrast == null) contrast = hdrFilter.applyConvolution3x3Filter(bmpImagesList);
        if (saturation == null) saturation = hdrFilter.applySaturationFilter(bmpImagesList);
        if (well_exposedness == null)
            well_exposedness = hdrFilter.applyExposureFilter(bmpImagesList);
        if (normal == null)
            normal = hdrFilter.computeNormalWeighted(contrast, saturation, well_exposedness);
        if (gaussian == null)
            gaussian = hdrFilter.generateGaussianPyramid(normal, HDRFilter.DATA_TYPE.FLOAT32);
        if (laplacian == null) laplacian = hdrFilter.generateLaplacianPyramids(bmpImagesList);
        if (resultant == null) {
            resultant = hdrFilter.generateResultant(gaussian, laplacian);
            collapse = hdrFilter.collapseResultant(resultant);
        }

        switch (action) {
            case CONTRAST:
                return HDRFilter.convertAllocationToBMP(contrast, HDRFilter.DATA_TYPE.FLOAT32);
            case SATURATION:
                return HDRFilter.convertAllocationToBMP(saturation, HDRFilter.DATA_TYPE.FLOAT32);
            case EXPOSED:
                return HDRFilter.convertAllocationToBMP(well_exposedness, HDRFilter.DATA_TYPE.FLOAT32);
            case NORMAL:
                return HDRFilter.convertAllocationToBMP(normal, HDRFilter.DATA_TYPE.FLOAT32);
            case GAUSSIAN:
                return HDRFilter.convertAllocationToBMP(gaussian.get(SELECTED_INDEX), HDRFilter.DATA_TYPE.FLOAT32_4);
            case LAPLACIAN:
                return HDRFilter.convertAllocationToBMP(laplacian.get(SELECTED_INDEX), HDRFilter.DATA_TYPE.FLOAT32_4);
            case RESULTANT:
                return HDRFilter.convertAllocationToBMP(resultant, HDRFilter.DATA_TYPE.FLOAT32_4);
            case COLLAPSE:
                return HDRFilter.convertAllocationToBMP(collapse, HDRFilter.DATA_TYPE.FLOAT32_4);
        }
        return null;
    }

    @Override
    public List<Bitmap> perform(List<Bitmap> bmpImagesList, Actions action, int selected) {
        SELECTED_INDEX = selected;
        return perform(bmpImagesList, action);
    }
}
