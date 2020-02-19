package com.adrino.renderscript;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.renderscript.Allocation;

import java.util.List;

public class ExposureFusion implements HDRManager.Presenter {

    public static boolean MEM_BOOST;
    public static final int SAMPLE_SIZE = 1;
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

    enum Actions {CONTRAST, SATURATION, EXPOSED, NORMAL, GAUSSIAN, LAPLACIAN, RESULTANT, COLLAPSE}


    ExposureFusion(Context context) {
        if (hdrFilter == null)
            hdrFilter = new HDRFilter(context);
    }

    @Override
    public void setMeta(int imgWidth, int imgHeight, Bitmap.Config imgConfig) {
        hdrFilter.setMeta(imgWidth, imgHeight, imgConfig);
    }

    @Override
    public List<Bitmap> perform(List<Bitmap> bmpImagesList, Actions action) {

        Log.e(TAG, "perform: contrast : "+contrast);
        if (contrast == null) contrast = hdrFilter.applyConvolution3x3Filter(bmpImagesList);
        if (saturation == null) saturation = hdrFilter.applySaturationFilter(bmpImagesList);
        if (well_exposedness == null)
            well_exposedness = hdrFilter.applyExposureFilter(bmpImagesList);
        if (normal == null && contrast != null && saturation != null && well_exposedness != null)
            normal = hdrFilter.computeNormalWeighted(contrast, saturation, well_exposedness);
        if (gaussian == null && normal != null)
            gaussian = hdrFilter.generateGaussianPyramid(normal, HDRFilter.DATA_TYPE.FLOAT32);
        if (laplacian == null) laplacian = hdrFilter.generateLaplacianPyramids(bmpImagesList);
        if (resultant == null && gaussian != null && laplacian != null) {
            resultant = hdrFilter.generateResultant(gaussian, laplacian);
        }
        if (collapse == null && resultant != null) {
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
                return hdrFilter.convertAllocationBMPDyamic(gaussian.get(SELECTED_INDEX));
            case LAPLACIAN:
                return hdrFilter.convertAllocationBMPDyamic(laplacian.get(SELECTED_INDEX));
            case RESULTANT:
                return hdrFilter.convertAllocationBMPDyamic(resultant);
            case COLLAPSE:
                return hdrFilter.convertAllocationBMPDyamic(collapse);
        }
        return null;
    }

    @Override
    public List<Bitmap> perform(List<Bitmap> bmpImagesList, Actions action, int selected) {
        SELECTED_INDEX = selected;
        return perform(bmpImagesList, action);
    }

//    List<Bitmap> gaussianPyramid(List<Bitmap> inImage, Actions action, int selected){
//        SELECTED_INDEX = selected;
//        return hdrFilter.convertAllocationBMPDyamic(hdrFilter.generateGaussianPyramid(hdrFilter.computeNormalWeighted(
//                hdrFilter.applyConvolution3x3Filter(inImage),
//                hdrFilter.applySaturationFilter(inImage),
//                hdrFilter.applyExposureFilter(inImage)
//        ), HDRFilter.DATA_TYPE.FLOAT32).get(SELECTED_INDEX));
//    }
//
//    List<Bitmap> laplacianPyramid(List<Bitmap> inImage, Actions action, int selected){
//        SELECTED_INDEX = selected;
//        return hdrFilter.convertAllocationBMPDyamic(hdrFilter.generateLaplacianPyramids(inImage).get(SELECTED_INDEX));
//    }
}