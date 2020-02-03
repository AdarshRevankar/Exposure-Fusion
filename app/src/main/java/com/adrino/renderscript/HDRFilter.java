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
    private ScriptC_WeightMap scriptWeightMap;


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
    public Bitmap computeWeightedFilter(Bitmap bmpContrast, Bitmap bmpSaturation, Bitmap bmpExposure) {
        scriptWeightMap = new ScriptC_WeightMap(renderScript);

        int bitmapWidth = bmpContrast.getWidth();
        int bitmapHeight = bmpContrast.getHeight();

        // Allocate
        Allocation inC = Allocation.createFromBitmap(renderScript, bmpContrast);
        Allocation inS = Allocation.createFromBitmap(renderScript, bmpSaturation);
        Allocation inE = Allocation.createFromBitmap(renderScript, bmpExposure);
        Allocation outAllocation = Allocation.createFromBitmap(renderScript, bmpContrast);

        // Script
        scriptWeightMap.set_inC(inC);
        scriptWeightMap.set_inS(inS);
        scriptWeightMap.set_inE(inE);
        scriptWeightMap.forEach_computeWeight(outAllocation);

        // Output
        Bitmap outBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, bmpContrast.getConfig());
        outAllocation.copyTo(outBitmap);

        //Destroy
        inC.destroy();
        inS.destroy();
        inE.destroy();
        outAllocation.destroy();
        scriptWeightMap.destroy();

        return outBitmap;
    }

    @Override
    public Bitmap computeNormalWeighted(Bitmap[] bmpImages) {
        Bitmap[] bmpWeighted = new Bitmap[3];

        // Calculate Weighted Images
        for (int i = 0; i < 3; i++) {
            bmpWeighted[i] = computeWeightedFilter(
                    applyConvolution3x3Filter(applyGrayScaleFilter(bmpImages[i])),
                    applySaturationFilter(bmpImages[i]),
                    applyExposureFilter(bmpImages[i])
            );
        }

        // Normalize Weighted Images
        // Allocate
        int bitmapWidth = bmpWeighted[0].getWidth();
        int bitmapHeight = bmpWeighted[0].getHeight();

        Allocation w1Alloc = Allocation.createFromBitmap(renderScript, bmpWeighted[0]);
        Allocation w2Alloc = Allocation.createFromBitmap(renderScript, bmpWeighted[1]);
        Allocation w3Alloc = Allocation.createFromBitmap(renderScript, bmpWeighted[2]);
        Allocation outAlloc = Allocation.createFromBitmap(renderScript, bmpImages[0]);

        ScriptC_NormalizeWeights scriptNW = new ScriptC_NormalizeWeights(renderScript);
        scriptNW.set_w1(w1Alloc);
        scriptNW.set_w2(w2Alloc);
        scriptNW.set_w3(w3Alloc);
        scriptNW.forEach_normalizeWeights(outAlloc);

        // Output
        Bitmap outBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, bmpWeighted[0].getConfig());
        outAlloc.copyTo(outBitmap);

        // Destory
        outAlloc.destroy();
        w1Alloc.destroy();
        w2Alloc.destroy();
        w3Alloc.destroy();
        scriptNW.destroy();

        return outBitmap;
    }

    @Override
    public void destoryRenderScript() {
        renderScript.destroy();
    }
}
