package com.adrino.hdr.corehdr;

import android.graphics.Bitmap;

import androidx.renderscript.Allocation;

import java.util.List;

public interface HDRManager {

    interface HDRClient {
        List<Bitmap> perform(List<Bitmap> bmpImagesList, CreateHDR.Actions action);
        List<Bitmap> perform(List<Bitmap> bmpImagesList, CreateHDR.Actions action, int selected);
        void destroy();
    }

    interface HDRProcessor {
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
        List<List<Allocation>> generateGaussianPyramid(List<Allocation> floatAlloc, Constants.DataType dataType);
        List<List<Allocation>> generateLaplacianPyramids(List<Bitmap> bmpImages);

        List<Allocation> generateResultant(List<List<Allocation>> gaussianPyramids, List<List<Allocation>> laplacianPyramids);
        List<Allocation> collapseResultant(List<Allocation> resultant);
        void destroy();
    }
}
