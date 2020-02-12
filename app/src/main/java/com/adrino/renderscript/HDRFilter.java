package com.adrino.renderscript;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.ScriptIntrinsicBlur;
import androidx.renderscript.ScriptIntrinsicConvolve3x3;

import java.util.ArrayList;
import java.util.List;

/**
 * Filter Class which has requried HDR Functions
 * Contains functions:
 * 1. Convolve
 * 2. RGBtoGray
 * 3. Saturation
 * 4. Exposure
 * 5. WeightedBitmap
 * 6. Normalization
 * 7. Product formation
 * 8. Collapse
 */
public class HDRFilter implements HDRManager.Performer {

    // Constants
    private final static String TAG = "HDRFilter";
    private static int PYRAMID_LEVELS = 4;

    // Attributes
    private static RenderScript renderScript;

    private static ScriptC_utils scriptUtils;
    private ScriptIntrinsicConvolve3x3 scriptConv;
    private ScriptC_Saturation scriptSaturation;
    private ScriptC_Exposure scriptExposure;
    private ScriptC_NormalizeWeights scriptNorm;
    private ScriptC_Laplacian scriptLaplacian;
    private ScriptC_Collapse scriptCollapse;
    private ScriptC_Gaussian scriptGaussian;
    private Element elementFloat4, elementFloat;

    enum DATA_TYPE {FLOAT32_4, FLOAT32}

    ;

    // Image meta
    private static Bitmap.Config config;
    private static int width, height;

    // Methods
    HDRFilter(Context context) {
        renderScript = RenderScript.create(context);
        elementFloat4 = Element.F32_4(renderScript);
        elementFloat = Element.F32(renderScript);
    }

    @Override
    public void setMeta(int imWidth, int imHeight, Bitmap.Config imConfig) {
        this.width = imWidth;
        this.height = imHeight;
        this.config = imConfig;
    }

    @Override
    public List<Allocation> applyConvolution3x3Filter(List<Bitmap> bmpImages) {
        // - - - - - - - - - - - - - - - - - - - - -
        //      Perform Convolution
        // - - - - - - - - - - - - - - - - - - - - -
        float[] filter = {0, 1, 0, 1, -4, 1, 0, 1, 0};                                  // Filter
        scriptConv = ScriptIntrinsicConvolve3x3.create(renderScript, elementFloat);     // Convolution intrinsic
        scriptUtils = new ScriptC_utils(renderScript);                                  // For Gray Scale image

        Allocation inAlloc, grayAlloc, outAlloc;
        List<Allocation> outAllocList = new ArrayList<>(3);

        for (Bitmap inImage : bmpImages) {
            inAlloc = Allocation.createFromBitmap(renderScript, inImage);
            grayAlloc = RsUtils.create2d(renderScript, width, height, elementFloat);
            outAlloc = RsUtils.create2d(renderScript, width, height, elementFloat);

            // Bitmap -> RGB
            scriptUtils.set_inGrayAlloc(inAlloc);
            scriptUtils.forEach_convertRGBAToGray(grayAlloc);

            // RGB -> Convoluted
            scriptConv.setInput(grayAlloc);
            scriptConv.setCoefficients(filter);
            scriptConv.forEach(outAlloc);

            // Add it to List
            outAllocList.add(outAlloc);

            inAlloc.destroy();
            grayAlloc.destroy();
        }
        scriptConv.destroy();
        scriptUtils.destroy();
        return outAllocList;
    }

    @Override
    public List<Allocation> applySaturationFilter(List<Bitmap> bmpImages) {
        // - - - - - - - - - - - - - - - - - - -
        //          Apply Saturation
        // - - - - - - - - - - - - - - - - - - -
        scriptSaturation = new ScriptC_Saturation(renderScript);

        List<Allocation> outAllocList = new ArrayList<>(3);
        Allocation inAllocation, outAllocation;

        for (Bitmap inImage : bmpImages) {
            inAllocation = Allocation.createFromBitmap(renderScript, inImage);
            outAllocation = RsUtils.create2d(renderScript, width, height, elementFloat);

            // Perform Saturation Script
            scriptSaturation.set_inAlloc(inAllocation);
            scriptSaturation.forEach_saturate(outAllocation);

            outAllocList.add(outAllocation);

            // Destroy
            inAllocation.destroy();
        }

        scriptSaturation.destroy();
        return outAllocList;
    }

    @Override
    public List<Allocation> applyExposureFilter(List<Bitmap> bmpImages) {
        // - - - - - - - - - - - - - - - - - - -
        //          Apply Exposedness
        // - - - - - - - - - - - - - - - - - - -
        scriptExposure = new ScriptC_Exposure(renderScript);

        List<Allocation> outAllocList = new ArrayList<>(3);
        Allocation inAllocation, outAllocation;

        for (Bitmap inImage : bmpImages) {
            // Allocate
            inAllocation = Allocation.createFromBitmap(renderScript, inImage);
            outAllocation = RsUtils.create2d(renderScript, width, height, elementFloat);

            // Perform
            scriptExposure.set_inAllocation(inAllocation);
            scriptExposure.forEach_expose(outAllocation);

            outAllocList.add(outAllocation);

            inAllocation.destroy();
        }
        scriptExposure.destroy();

        return outAllocList;
    }

    @Override
    public List<Allocation> computeNormalWeighted(List<Allocation> contrast,
                                                  List<Allocation> saturation,
                                                  List<Allocation> wellExposeness) {

        // - - - - - - - - - - - - - - - -
        //          Normal Weight
        // - - - - - - - - - - - - - - - -
        scriptNorm = new ScriptC_NormalizeWeights(renderScript);

        Allocation outAlloc1 = RsUtils.create2d(renderScript, width, height, elementFloat);
        Allocation outAlloc2 = RsUtils.create2d(renderScript, width, height, elementFloat);
        Allocation outAlloc3 = RsUtils.create2d(renderScript, width, height, elementFloat);

        scriptNorm.set_out2(outAlloc2);
        scriptNorm.set_out3(outAlloc3);

        scriptNorm.set_C1(contrast.get(0));
        scriptNorm.set_C2(contrast.get(1));
        scriptNorm.set_C3(contrast.get(2));

        scriptNorm.set_S1(saturation.get(0));
        scriptNorm.set_S2(saturation.get(1));
        scriptNorm.set_S3(saturation.get(2));

        scriptNorm.set_E1(wellExposeness.get(0));
        scriptNorm.set_E2(wellExposeness.get(1));
        scriptNorm.set_E3(wellExposeness.get(2));

        // Compute
        scriptNorm.forEach_normalizeWeights(outAlloc1);

        List<Allocation> outAllocList = new ArrayList<>(3);
        outAllocList.add(outAlloc1);
        outAllocList.add(outAlloc2);
        outAllocList.add(outAlloc3);

        scriptNorm.destroy();

        return outAllocList;
    }

    @Override
    public List<List<Allocation>> generateGaussianPyramid(List<Bitmap> bmpImageList) {
        // - - - - - - - - - - - - - - - - - - - -
        // RenderScript - Gaussian.rs ( Convolve )
        // - - - - - - - - - - - - - - - - - - - -
        scriptGaussian = new ScriptC_Gaussian(renderScript);
        scriptUtils = new ScriptC_utils(renderScript);
        PYRAMID_LEVELS = 6;

        Log.e(TAG, "generateGaussianPyramid: Number of Pyramids =" + PYRAMID_LEVELS);

        List<List<Allocation>> outGaussianAllocationList = new ArrayList<>(3);

        Allocation inAlloc, outAlloc, midAlloc, expandInAlloc, expandOutAlloc, convertAlloc, startAlloc;

        for (int i = 0; i < bmpImageList.size(); i++) {
            List<Allocation> outGaussLevelList = new ArrayList<>(PYRAMID_LEVELS);

            // - - - - - - Allocation - - - - - - -
            // Convert U4 to F4
            startAlloc = Allocation.createFromBitmap(renderScript, bmpImageList.get(i));
            convertAlloc = RsUtils.create2d(renderScript, width, height, Element.F32_4(renderScript));
            inAlloc = RsUtils.create2d(renderScript, width, height, Element.F32_4(renderScript));
            midAlloc = RsUtils.create2d(renderScript, width, height, Element.F32_4(renderScript));
            outAlloc = RsUtils.create2d(renderScript, width, height, Element.F32_4(renderScript));

            scriptUtils.set_inAlloc(startAlloc);
            scriptUtils.forEach_convertU4toF4(convertAlloc);
            startAlloc.destroy();

            // - - - - - - Computation - - - - - -

            // G0 = Original Image
            inAlloc.copyFrom(convertAlloc);
            outGaussLevelList.add(convertAlloc);

            for (int level = 1; level < PYRAMID_LEVELS; level++) {

                // REDUCE
                int compressDenom = (int) Math.pow(2, level);
                scriptGaussian.set_compressTargetWidth(width / compressDenom);
                scriptGaussian.set_compressTargetHeight(height / compressDenom);

                scriptGaussian.set_compressSource(inAlloc);
                scriptGaussian.forEach_compressFloat4Step1(midAlloc);
                scriptGaussian.set_compressSource(midAlloc);
                scriptGaussian.forEach_compressFloat4Step2(outAlloc);

                expandInAlloc = RsUtils.create2d(renderScript, width, height, Element.F32_4(renderScript));
                expandOutAlloc = RsUtils.create2d(renderScript, width, height, Element.F32_4(renderScript));

                expandInAlloc.copyFrom(outAlloc);
                inAlloc.copyFrom(outAlloc);

                // EXPAND
                for (int j = level - 1; j >= 0; j--) {
                    int expandDenom = (int) Math.pow(2, i);
                    scriptGaussian.set_expandTargetWidth(width * expandDenom);
                    scriptGaussian.set_expandTargetHeight(height * expandDenom);

                    scriptGaussian.set_expandSource(expandInAlloc);
                    scriptGaussian.forEach_expandFloat4Step1(midAlloc);
                    scriptGaussian.set_expandSource(midAlloc);
                    scriptGaussian.forEach_expandFloat4Step2(expandOutAlloc);

                    expandInAlloc.copyFrom(expandOutAlloc);
                }

                // Store Result
                expandInAlloc.destroy();
                outGaussLevelList.add(expandOutAlloc);
            }
            midAlloc.destroy();
            outGaussianAllocationList.add(outGaussLevelList);
        }

        // 3. - - - Destroy & Return - - - -
        return outGaussianAllocationList;
    }

    @Override
    public List<List<Allocation>> generateGaussianPyramid(List<Allocation> floatAlloc, DATA_TYPE data_type) {
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        // RenderScript - Gaussian.rs ( Convolve ) Float Allocation
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        return generateGaussianPyramid(convertAllocationToBMP(floatAlloc, DATA_TYPE.FLOAT32));
    }


    @Override
    public List<List<Allocation>> generateLaplacianPyramids(List<Bitmap> bmpInMultiExposures) {
        // + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
        // |    Laplacian Pyramid - laplacian.rs ( Diff b/n Gaussian Pyr.)   |
        // + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
        scriptLaplacian = new ScriptC_Laplacian(renderScript);

        // - - - - Generate Gaussian Pyramid - - - - - -
        List<List<Allocation>> gaussPyramidList = generateGaussianPyramid(bmpInMultiExposures);

        // - - - - Output Images - - - - - - - - - - - -
        List<List<Allocation>> laplacianPyramidList = new ArrayList<>(gaussPyramidList.size());

        // - - - - Script - - - - - - - - - - - - - - - -
        for (int i = 0; i < gaussPyramidList.size(); i++) {

            // - - - - Buffer Allocation - - - - - -
            List<Allocation> inGauss = gaussPyramidList.get(i);
            List<Allocation> outLap = new ArrayList<>(PYRAMID_LEVELS);

            // - - - - - LAPLACIAN PYRAMID : L0 = G0 - G1
            int lapLevel = 0;
            for (; lapLevel < PYRAMID_LEVELS - 1; lapLevel++) {
                Allocation outAlloc = RsUtils.create2d(renderScript, width, height, elementFloat4);
                scriptLaplacian.set_laplacianLowerLevel(inGauss.get(lapLevel));
                scriptLaplacian.forEach_laplacian(inGauss.get(lapLevel + 1), outAlloc);
                outLap.add(outAlloc);
            }

            // - - - - - L(N) - - - - - - - - - -
            outLap.add(inGauss.get(lapLevel));

            // Attach to List
            laplacianPyramidList.add(outLap);
        }
        scriptLaplacian.destroy();

        return laplacianPyramidList;
    }

    @Override
    public List<Allocation> generateResultant(List<List<Allocation>> gaussianPyramids, List<List<Allocation>> laplacianPyramids) {

        // Check, |Gi| == |Li|
        if (gaussianPyramids.size() != laplacianPyramids.size()) {
            Log.e(TAG, "generateResultant: Parameter Length not equal | PARAM_LENGTH_ERROR ");
        }

        // - - - - - - - - - - - - - - - - - - -
        //          RESULTANT GENERATOR
        // - - - - - - - - - - - - - - - - - - -
        scriptCollapse = new ScriptC_Collapse(renderScript);

        List<Allocation> resultantPyramid = new ArrayList<>(PYRAMID_LEVELS);

        // - - - - For Each level - - - - -
        for (int level = 0; level < PYRAMID_LEVELS; level++) {
            Allocation outAlloc = RsUtils.create2d(renderScript, width, height, elementFloat4);

            // - - - - Script - - - - -
            scriptCollapse.set_GP1(gaussianPyramids.get(0).get(level));
            scriptCollapse.set_GP2(gaussianPyramids.get(1).get(level));
            scriptCollapse.set_GP3(gaussianPyramids.get(2).get(level));
            scriptCollapse.set_LP1(laplacianPyramids.get(0).get(level));
            scriptCollapse.set_LP2(laplacianPyramids.get(1).get(level));
            scriptCollapse.set_LP3(laplacianPyramids.get(2).get(level));
            scriptCollapse.forEach_multiplyBMP(outAlloc);

            resultantPyramid.add(outAlloc);
        }

        return resultantPyramid;
    }

    @Override
    public Bitmap collapseResultant(Bitmap[] resultant) {

        scriptCollapse = new ScriptC_Collapse(renderScript);

        Allocation inAllocation = Allocation.createFromBitmap(renderScript, resultant[0]);
        Allocation middleResultAllocation = Allocation.createFromBitmap(renderScript, resultant[1]);
        Allocation outAllocation = Allocation.createFromBitmap(renderScript, resultant[0]);

        scriptCollapse.set_collapseLevel(middleResultAllocation);
        scriptCollapse.forEach_collapse(inAllocation, outAllocation);
        middleResultAllocation.copyFrom(outAllocation);
        inAllocation.copyFrom(resultant[2]);

        scriptCollapse.set_collapseLevel(middleResultAllocation);
        scriptCollapse.forEach_collapse(inAllocation, outAllocation);
        middleResultAllocation.copyFrom(outAllocation);
        inAllocation.copyFrom(resultant[3]);

        scriptCollapse.set_collapseLevel(middleResultAllocation);
        scriptCollapse.forEach_collapse(inAllocation, outAllocation);

        Bitmap hdrOutput = Bitmap.createBitmap(width, height, resultant[0].getConfig());
        outAllocation.copyTo(hdrOutput);
        return hdrOutput;
    }

    static List<Bitmap> convertAllocationToBMP(List<Allocation> inAllocList, DATA_TYPE data_type) {
        scriptUtils = new ScriptC_utils(renderScript);

        Allocation outAlloc;
        Bitmap outBmp;
        List<Bitmap> outBmpList = new ArrayList<>(inAllocList.size());

        for (int i = 0; i < inAllocList.size(); i++) {
            // Allocate
            outBmp = Bitmap.createBitmap(width, height, config);
            outAlloc = Allocation.createFromBitmap(renderScript, outBmp);

            // Perform
            scriptUtils.set_inAlloc(inAllocList.get(i));

            if (data_type == DATA_TYPE.FLOAT32) {
                scriptUtils.forEach_convertFtoU4(outAlloc);
            } else {
                scriptUtils.forEach_convertF4toU4(outAlloc);
            }

            outAlloc.copyTo(outBmp);
            outBmpList.add(outBmp);

            outAlloc.destroy();
        }
        scriptUtils.destroy();
        return outBmpList;
    }


}

//    @Override
//    public Bitmap applyConvolution3x3Filter(Bitmap bmpImage) {
//        // - - - - - - - - - - - - - - - - - - - - -
//        //      Perform Convolution
//        // - - - - - - - - - - - - - - - - - - - - -
//        float[] filter = {0, 1, 0, 1, -4, 1, 0, 1, 0};
//        scriptConv = ScriptIntrinsicConvolve3x3.create(renderScript, elementFloat);
//        scriptUtils = new ScriptC_utils(renderScript);
//
//        width = bmpImage.getWidth();
//        height = bmpImage.getHeight();
//        config = bmpImage.getConfig();
//
//        Allocation inAlloc = Allocation.createFromBitmap(renderScript, bmpImage);
//        Allocation grayAlloc = RsUtils.create2d(renderScript, width, height, elementFloat);
//        Allocation outAlloc = RsUtils.create2d(renderScript, width, height, elementFloat);
//
//
//        scriptUtils.set_inGrayAlloc(inAlloc);
//        scriptUtils.forEach_convertRGBAToGray(grayAlloc);
//
//        scriptConv.setInput(grayAlloc);
//        scriptConv.setCoefficients(filter);
//        scriptConv.forEach(outAlloc);
//
//        inAlloc.destroy();
//        grayAlloc.destroy();
//
//        return convertAllocationToBitmap(outAlloc, bmpImage, elementFloat);
//    }

//    @Override
//    public Bitmap applyExposureFilter(Bitmap bmpImage) {
//        scriptExposure = new ScriptC_Exposure(renderScript);
//
//        int bitmapWidth = bmpImage.getWidth();
//        int bitmapHeight = bmpImage.getHeight();
//
//        // Allocate
//        Allocation inAllocation = Allocation.createFromBitmap(renderScript, bmpImage);
//        Allocation outAllocation = Allocation.createFromBitmap(renderScript, bmpImage);
//
//        // Script
//        scriptExposure.set_inAllocation(inAllocation);
//        scriptExposure.forEach_expose(outAllocation);
//
//        // Output
//        Bitmap outBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, bmpImage.getConfig());
//        outAllocation.copyTo(outBitmap);
//
//        //Destroy
//        inAllocation.destroy();
//        outAllocation.destroy();
//        scriptExposure.destroy();
//
//        return outBitmap;
//    }
