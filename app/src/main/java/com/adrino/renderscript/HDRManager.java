package com.adrino.renderscript;

import android.graphics.Bitmap;

import androidx.renderscript.Allocation;

import java.util.List;

public interface HDRManager {
    interface Performer {
        // Init
        void setMeta(int imgWidth, int imgHeight, Bitmap.Config imgConfig);

        // Required Methods
        List<Allocation> applyConvolution3x3Filter(List<Bitmap> bmpImages);
        List<Allocation> applySaturationFilter(List<Bitmap> bmpImages);
        List<Allocation> applyExposureFilter(List<Bitmap> bmpImages);

        List<Allocation> computeNormalWeighted(List<Allocation> contrast,
                                               List<Allocation> saturation,
                                               List<Allocation> well_exposedness);
        // Pyramids
        List<List<Allocation>> generateGaussianPyramid(List<Bitmap> bmpImageList);
        List<List<Allocation>> generateGaussianPyramid(List<Allocation> floatAlloc, HDRFilter.DATA_TYPE data_type);
        List<List<Allocation>> generateLaplacianPyramids(List<Bitmap> bmpImages);

        List<Allocation> generateResultant(List<List<Allocation>> gaussianPyramids, List<List<Allocation>> laplacianPyramids);
        Bitmap collapseResultant(Bitmap[] resultant);
    }

    interface Presenter{
        List<Bitmap> perform(List<Bitmap> bmpImagesList, ExposureFusion.Actions action);
        List<Bitmap> perform(List<Bitmap> bmpImagesList, ExposureFusion.Actions action, int selected);
        void setMeta(int imgWidth, int imgHeight, Bitmap.Config imgConfig);
    }

    interface Viewer{

    }
}
