package com.adrino.renderscript;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;

public class HDRFilter implements HDRManager.Presenter{

    // Attributes
    private static RenderScript renderScript;
    private ScriptC_RGBtoGray scriptGray;
    private ScriptC_Convolve scriptConvolve;
    private ScriptC_Saturation scriptSaturation;
    private ScriptC_Exposure scriptExposure;
    private ScriptC_NormalizeWeights scriptNorm;

    private static float[] laplacianKernel = {
            0.f, 1.f, 0.f,
            1.f, -4.f,1.f,
            0.f, 1.f, 0.f
    };

    private static float[] gaussianKernel = {
            0.0625f, 0.125f, 0.0625f,
            0.125f, 0.25f, 0.125f,
            0.0625f, 0.125f, 0.0625f
    };

    // Methods
    HDRFilter(Context context){
        renderScript = RenderScript.create(context);
    }

    @Override
    public Bitmap applyGrayScaleFilter(Bitmap bmpImage) {
        scriptGray = new ScriptC_RGBtoGray(renderScript);

        int bitmapWidth = bmpImage.getWidth();
        int bitmapHeight = bmpImage.getHeight();

        // Allocate
        Allocation inAllocation = Allocation.createFromBitmap(renderScript, bmpImage);
        Allocation outAllocation = Allocation.createFromBitmap(renderScript, bmpImage);

        // Script
        scriptGray.set_inAllocation(inAllocation);
        scriptGray.forEach_convertRGBAToGray(outAllocation);


        Bitmap outBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, bmpImage.getConfig());
        outAllocation.copyTo(outBitmap);

        //Destroy
        inAllocation.destroy();
        outAllocation.destroy();
        scriptGray.destroy();

        return outBitmap;
    }

    @Override
    public Bitmap applyConvolution3x3Filter(Bitmap bmpImage) {
        scriptConvolve = new ScriptC_Convolve(renderScript);

        int bitmapWidth = bmpImage.getWidth();
        int bitmapHeight = bmpImage.getHeight();

        // Allocate
        Allocation inAllocation = Allocation.createFromBitmap(renderScript, bmpImage);
        Allocation outAllocation = Allocation.createFromBitmap(renderScript, bmpImage);
        Allocation kernalAllocation = Allocation.createSized(renderScript,
                                                                Element.F32(renderScript),
                                                                laplacianKernel.length);

        // Script
        kernalAllocation.copyFrom(laplacianKernel);
        scriptConvolve.bind_conv_kernel(kernalAllocation);
        scriptConvolve.set_gIn(inAllocation);
        scriptConvolve.invoke_setup();
        scriptConvolve.forEach_root(inAllocation, outAllocation);


        Bitmap outBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, bmpImage.getConfig());
        outAllocation.copyTo(outBitmap);

        //Destroy
        inAllocation.destroy();
        outAllocation.destroy();
        scriptConvolve.destroy();

        return outBitmap;
    }

    @Override
    public Bitmap applySaturationFilter(Bitmap bmpImage) {

        scriptSaturation = new ScriptC_Saturation(renderScript);


        int bitmapWidth = bmpImage.getWidth();
        int bitmapHeight = bmpImage.getHeight();

        // Allocate
        Allocation inAllocation = Allocation.createFromBitmap(renderScript, bmpImage);
        Allocation outAllocation = Allocation.createFromBitmap(renderScript, bmpImage);

        // Script
        scriptSaturation.set_inAlloc(inAllocation);
        scriptSaturation.forEach_saturate(outAllocation);

        Bitmap outBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, bmpImage.getConfig());
        outAllocation.copyTo(outBitmap);

        //Destroy
        inAllocation.destroy();
        outAllocation.destroy();
        scriptSaturation.destroy();

        return outBitmap;
    }

    @Override
    public Bitmap applyExposureFilter(Bitmap bmpImage) {
        scriptExposure = new ScriptC_Exposure(renderScript);

        int bitmapWidth = bmpImage.getWidth();
        int bitmapHeight = bmpImage.getHeight();

        // Allocate
        Allocation inAllocation = Allocation.createFromBitmap(renderScript, bmpImage);
        Allocation outAllocation = Allocation.createFromBitmap(renderScript, bmpImage);

        // Script
        scriptExposure.set_inAllocation(inAllocation);
        scriptExposure.forEach_expose(outAllocation);

        // Output
        Bitmap outBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, bmpImage.getConfig());
        outAllocation.copyTo(outBitmap);

        //Destroy
        inAllocation.destroy();
        outAllocation.destroy();
        scriptExposure.destroy();

        return outBitmap;
    }

    @Override
    public Bitmap[] computeNormalWeighted(Bitmap[] bmpImages) {
        Bitmap[] bmpListContrast = new Bitmap[3];
        Bitmap[] bmpListSaturation = new Bitmap[3];
        Bitmap[] bmpListExposure = new Bitmap[3];

        int bitmapWidth = bmpImages[0].getWidth();
        int bitmapHeight = bmpImages[0].getHeight();

        // Calculate Weighted Images
        for (int i = 0; i < 3; i++) {
            bmpListContrast[i] = applyConvolution3x3Filter(applyGrayScaleFilter(bmpImages[i]));
            bmpListSaturation[i] = applySaturationFilter(bmpImages[i]);
            bmpListExposure[i] = applyExposureFilter(bmpImages[i]);
        }

        // Allocate & Execute
        Allocation c1 = Allocation.createFromBitmap(renderScript, bmpListContrast[0]);
        Allocation c2 = Allocation.createFromBitmap(renderScript, bmpListContrast[1]);
        Allocation c3 = Allocation.createFromBitmap(renderScript, bmpListContrast[2]);

        Allocation s1 = Allocation.createFromBitmap(renderScript, bmpListSaturation[0]);
        Allocation s2 = Allocation.createFromBitmap(renderScript, bmpListSaturation[1]);
        Allocation s3 = Allocation.createFromBitmap(renderScript, bmpListSaturation[2]);

        Allocation e1 = Allocation.createFromBitmap(renderScript, bmpListExposure[0]);
        Allocation e2 = Allocation.createFromBitmap(renderScript, bmpListExposure[1]);
        Allocation e3 = Allocation.createFromBitmap(renderScript, bmpListExposure[2]);

        Allocation outAlloc1 = Allocation.createFromBitmap(renderScript, bmpImages[0]);
        Allocation outAlloc2 = Allocation.createFromBitmap(renderScript, bmpImages[0]);
        Allocation outAlloc3 = Allocation.createFromBitmap(renderScript, bmpImages[0]);

        scriptNorm = new ScriptC_NormalizeWeights(renderScript);

        scriptNorm.set_nImageOut2(outAlloc2);
        scriptNorm.set_nImageOut3(outAlloc3);

        scriptNorm.set_C1(c1);
        scriptNorm.set_C2(c2);
        scriptNorm.set_C3(c3);

        scriptNorm.set_S1(s1);
        scriptNorm.set_S2(s2);
        scriptNorm.set_S3(s3);

        scriptNorm.set_E1(e1);
        scriptNorm.set_E2(e2);
        scriptNorm.set_E3(e3);

        // Compute
        scriptNorm.forEach_normalizeWeights(outAlloc1);

        // Prepare out Bitmaps
        Bitmap[] bmpOut = new Bitmap[3];
        for(int i = 0; i< 3; i++){
            bmpOut[i] = Bitmap.createBitmap(bitmapWidth, bitmapHeight, bmpImages[0].getConfig());
        }
        outAlloc1.copyTo(bmpOut[0]);
        outAlloc2.copyTo(bmpOut[1]);
        outAlloc3.copyTo(bmpOut[2]);

        return bmpOut;
    }

    @Override
    public void destoryRenderScript() {
        renderScript.destroy();
    }
}
