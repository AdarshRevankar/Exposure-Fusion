package com.adrino.renderscript;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.renderscript.Allocation;

import java.util.List;

public class ExposureFusion implements HDRManager.Presenter {

    static boolean MEM_BOOST;
    private static final String TAG = "ExposureFusion";
    private static int SELECTED_INDEX = 0;
    private static HDRFilter hdrFilter = null;
    private static List<Allocation> resultant = null;
    private static List<Allocation> collapse = null;
    private static List<Allocation> contrast = null;
    private static List<Allocation> saturation = null;
    private static List<Allocation> well_exposedness = null;
    private static List<Allocation> normal = null;
    private static List<List<Allocation>> gaussian = null;
    private static List<List<Allocation>> laplacian = null;

    void destroy() {
        if(contrast != null) {
            for (Allocation alloc :
                    contrast) {
                alloc.destroy();
            }
            contrast = null;
        }

        if(saturation != null) {
            for (Allocation alloc :
                    saturation) {
                alloc.destroy();
            }
            saturation = null;
        }

        if(well_exposedness != null) {
            for (Allocation alloc :
                    well_exposedness) {
                alloc.destroy();
            }
            well_exposedness = null;
        }

        if(normal != null) {
            for (Allocation alloc :
                    normal) {
                alloc.destroy();
            }
            normal = null;
        }

        if(collapse != null) {
            for (Allocation alloc :
                    collapse) {
                alloc.destroy();
            }
            collapse = null;
        }

        if(gaussian != null){
            for (List<Allocation> lst: gaussian) {
                for (Allocation alloc: lst) {
                    alloc.destroy();
                }
            }
            gaussian = null;
        }

        if(laplacian != null){
            for (List<Allocation> lst: laplacian) {
                for (Allocation alloc: lst) {
                    alloc.destroy();
                }
            }
            laplacian = null;
        }

        if(resultant != null) {
            for (Allocation alloc :
                    resultant) {
                alloc.destroy();
            }
            resultant = null;
        }
    }

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

        Log.e(TAG, "perform: contrast : " + contrast);
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
                if(resultant != null && collapse != null)
                    return hdrFilter.convertAllocationBMPDyamic(collapse);
                else{
                    return hdrFilter.convertAllocationBMPDyamic(
                            hdrFilter.collapseResultant(
                                    hdrFilter.generateResultant(
                                            hdrFilter.generateGaussianPyramid(
                                                    hdrFilter.computeNormalWeighted(
                                                            hdrFilter.applyConvolution3x3Filter(bmpImagesList),
                                                            hdrFilter.applySaturationFilter(bmpImagesList),
                                                            hdrFilter.applyExposureFilter(bmpImagesList)
                                                    ), HDRFilter.DATA_TYPE.FLOAT32
                                            ),
                                            hdrFilter.generateLaplacianPyramids(bmpImagesList)
                                    )
                            )
                    );
                }
        }
        return null;
    }

    @Override
    public List<Bitmap> perform(List<Bitmap> bmpImagesList, Actions action, int selected) {
        SELECTED_INDEX = selected;
        return perform(bmpImagesList, action);
    }
}