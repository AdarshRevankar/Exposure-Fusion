package com.adrino.renderscript;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.renderscript.Allocation;

import java.util.List;

public class ExposureFusion implements HDRManager.Presenter {

    public static final int SAMPLE_SIZE = 6;
    private static final String TAG = "ExposureFusion";
    private static int SELECTED_INDEX = 0;
    private static HDRFilter hdrFilter;
    private static List<Allocation> resultant;
    private static List<Allocation> collapse;
    private static List<Allocation> contrast;
    private static List<Allocation> saturation;
    private static List<Allocation> well_exposedness;
    private static List<Allocation> normal;
    private static List<List<Allocation>> gaussian;
    private static List<List<Allocation>> laplacian;

    enum Actions {CONTRAST, SATURATION, EXPOSED, NORMAL, GAUSSIAN, LAPLACIAN, RESULTANT, COLLAPSE};

    ExposureFusion(Context context) {
        if(hdrFilter == null)
            hdrFilter = new HDRFilter(context);
    }

    @Override
    public void setMeta(int imgWidth, int imgHeight, Bitmap.Config imgConfig) {
        hdrFilter.setMeta(imgWidth, imgHeight, imgConfig);
    }

    @Override
    public List<Bitmap> perform(List<Bitmap> bmpImagesList, Actions action) {

//        if (contrast == null) contrast = hdrFilter.applyConvolution3x3Filter(bmpImagesList);
//        if (saturation == null) saturation = hdrFilter.applySaturationFilter(bmpImagesList);
//        if (well_exposedness == null)
//            well_exposedness = hdrFilter.applyExposureFilter(bmpImagesList);
//        if (normal == null && contrast != null && saturation != null && well_exposedness != null)
        if(normal == null) normal = hdrFilter.computeNormalWeighted(hdrFilter.applyConvolution3x3Filter(bmpImagesList), hdrFilter.applySaturationFilter(bmpImagesList),  hdrFilter.applyExposureFilter(bmpImagesList));
        if (resultant == null) {
            resultant = hdrFilter.generateResultant(hdrFilter.generateGaussianPyramid(normal, HDRFilter.DATA_TYPE.FLOAT32), hdrFilter.generateLaplacianPyramids(bmpImagesList));
        }
        if(collapse == null && resultant != null){
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
