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

        Bitmap[] generateGaussianPyramid(Bitmap bmpImages);
        Bitmap[][] generateLaplacianPyramids(Bitmap[] bmpImages);
        Bitmap[] generateResultant(Bitmap[][] gaussianPyramids, Bitmap[][] laplacianPyramids);
        Bitmap collapseResultant(Bitmap[] resultant);
    }

    interface Presenter{
        List<Bitmap> perform(List<Bitmap> bmpImagesList, ExposureFusion.Actions action);
        void setMeta(int imgWidth, int imgHeight, Bitmap.Config imgConfig);
    }

    interface Viewer{

    }
}
