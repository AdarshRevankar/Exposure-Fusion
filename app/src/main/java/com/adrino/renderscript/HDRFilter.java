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
                                                  List<Allocation> well_exposedness) {

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

        scriptNorm.set_E1(well_exposedness.get(0));
        scriptNorm.set_E2(well_exposedness.get(1));
        scriptNorm.set_E3(well_exposedness.get(2));

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
        PYRAMID_LEVELS = (int) Math.floor(Math.log(Math.min(width, height)) / (Math.log(2) * 1.75));

        Log.e(TAG, "generateGaussianPyramid: " + PYRAMID_LEVELS);

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
        // - - - - - - - - - - - - - - - - - - - -
        // RenderScript - Gaussian.rs ( Convolve ) Float Allocation
        // - - - - - - - - - - - - - - - - - - - -


        scriptGaussian = new ScriptC_Gaussian(renderScript);
        scriptUtils = new ScriptC_utils(renderScript);

        PYRAMID_LEVELS = (int) Math.floor(Math.log(Math.min(width, height)) / (Math.log(2) * 1.75));

        Log.e(TAG, "generateGaussianPyramid: "+PYRAMID_LEVELS );

        List<List<Allocation>> outGaussianAllocationList = new ArrayList<>(3);

        Allocation inAlloc, outAlloc, midAlloc, expandInAlloc, expandOutAlloc, convertAlloc;

        for (int i = 0; i < floatAlloc.size(); i++) {
            List<Allocation> outGaussLevelList = new ArrayList<>(PYRAMID_LEVELS);

            // - - - - - - Allocation - - - - - - -
            // Convert U4 to F4
            convertAlloc = RsUtils.create2d(renderScript, width, height, Element.F32_4(renderScript));
            inAlloc = RsUtils.create2d(renderScript, width, height, Element.F32_4(renderScript));
            midAlloc = RsUtils.create2d(renderScript, width, height, Element.F32_4(renderScript));
            outAlloc = RsUtils.create2d(renderScript, width, height, Element.F32_4(renderScript));

            scriptUtils.set_inAlloc(floatAlloc.get(i));
            scriptUtils.forEach_convertFtoF4(convertAlloc);

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
                inAlloc = RsUtils.create2d(renderScript, width, height, Element.F32_4(renderScript));
                inAlloc.copyFrom(outAlloc);
                outAlloc.destroy();

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
                outAlloc = RsUtils.create2d(renderScript, width, height, Element.F32_4(renderScript));
            }
            midAlloc.destroy();
            outGaussianAllocationList.add(outGaussLevelList);
        }

        // 3. - - - Destroy & Return - - - -
        return outGaussianAllocationList;
    }


    @Override
    public Bitmap[][] generateLaplacianPyramids(Bitmap[] bmpInMultiExposures) {

        // - - - - - - - - - - - - - - - - - - - -
        // Obtain - 3 Gaussian Pyramids ( 3 BMP )
        // - - - - - - - - - - - - - - - - - - - -
        Bitmap[][] bmpGaussianPyramids = new Bitmap[bmpInMultiExposures.length][PYRAMID_LEVELS];
        for (int i = 0; i < bmpInMultiExposures.length; i++) {

            // Gaussian Pyramids
            //bmpGaussianPyramids[i] = generateGaussianPyramid(bmpInMultiExposures[i]);
        }

        // - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        // Laplacian Pyramid - laplacian.rs ( Diff b/n Gaussian Pyr.
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - -

        // - - - - Output Images - - - - - -
        Bitmap[][] bmpLaplacianPyramids = new Bitmap[bmpInMultiExposures.length][PYRAMID_LEVELS];

        for (int i = 0; i < bmpInMultiExposures.length; i++) {
            for (int j = 0; j < PYRAMID_LEVELS; j++) {
                bmpLaplacianPyramids[i][j] = Bitmap.createBitmap(width, height, bmpInMultiExposures[0].getConfig());
            }
        }

        // - - - - Script - - - - - -
        scriptLaplacian = new ScriptC_Laplacian(renderScript);

        Allocation G0 = Allocation.createFromBitmap(renderScript, bmpGaussianPyramids[0][0]);
        Allocation G1 = Allocation.createFromBitmap(renderScript, bmpGaussianPyramids[0][1]);
        Allocation G2 = Allocation.createFromBitmap(renderScript, bmpGaussianPyramids[0][2]);
        Allocation G3 = Allocation.createFromBitmap(renderScript, bmpGaussianPyramids[0][3]);
        Allocation outAlloc = Allocation.createFromBitmap(renderScript, bmpGaussianPyramids[0][0]);

        for (int i = 0; i < bmpGaussianPyramids.length; i++) {

            // - - - - Allocation - - - -
            G0.copyFrom(bmpGaussianPyramids[i][0]);
            G1.copyFrom(bmpGaussianPyramids[i][1]);
            G2.copyFrom(bmpGaussianPyramids[i][2]);
            G3.copyFrom(bmpGaussianPyramids[i][3]);

            // - - - - PRODUCE : L0 = G0 - G1 - - - -
            scriptLaplacian.set_laplacianLowerLevel(G0);
            scriptLaplacian.forEach_laplacian(G1, outAlloc);
            outAlloc.copyTo(bmpLaplacianPyramids[i][0]);

            // - - - - PRODUCE : L1 = G1 - G2 - - - -
            scriptLaplacian.set_laplacianLowerLevel(G1);
            scriptLaplacian.forEach_laplacian(G2, outAlloc);
            outAlloc.copyTo(bmpLaplacianPyramids[i][1]);

            // - - - - PRODUCE : L2 = G2 - G3 - - - -
            scriptLaplacian.set_laplacianLowerLevel(G2);
            scriptLaplacian.forEach_laplacian(G3, outAlloc);
            outAlloc.copyTo(bmpLaplacianPyramids[i][2]);

            // - - - - PRODUCE : L3 = G3 - - - -
            bmpLaplacianPyramids[i][3] = bmpGaussianPyramids[i][3];

        }

        scriptLaplacian.destroy();
        G0.destroy();
        G1.destroy();
        G2.destroy();
        G3.destroy();

        return bmpLaplacianPyramids;
    }

    @Override
    public Bitmap[] generateResultant(Bitmap[][] gaussianPyramids, Bitmap[][] laplacianPyramids) {

        // Check, |Gi| == |Li|
        if (gaussianPyramids.length != laplacianPyramids.length) {
            Log.e(TAG, "generateResultant: Parameter Length not equal | PARAM_LENGTH_ERROR ");
        }

        // - - - - - - - - - - - - - - - - - - -
        //          RESULTANT GENERATOR
        // - - - - - - - - - - - - - - - - - - -
        scriptCollapse = new ScriptC_Collapse(renderScript);

        Bitmap[] bmpResultantPyramid = new Bitmap[PYRAMID_LEVELS];

        int level = 0;
        Allocation GP1 = Allocation.createFromBitmap(renderScript, gaussianPyramids[0][level]);
        Allocation GP2 = Allocation.createFromBitmap(renderScript, gaussianPyramids[1][level]);
        Allocation GP3 = Allocation.createFromBitmap(renderScript, gaussianPyramids[2][level]);

        Allocation LP1 = Allocation.createFromBitmap(renderScript, laplacianPyramids[0][level]);
        Allocation LP2 = Allocation.createFromBitmap(renderScript, laplacianPyramids[1][level]);
        Allocation LP3 = Allocation.createFromBitmap(renderScript, laplacianPyramids[2][level]);

        Allocation outAlloc = Allocation.createFromBitmap(renderScript, gaussianPyramids[0][level]);

        // - - - - For Each level - - - - -
        for (level = 0; level < PYRAMID_LEVELS; level++) {

            // - - - - Allocate - - - - -
            if (level > 0) {
                GP1.copyFrom(gaussianPyramids[0][level]);
                GP2.copyFrom(gaussianPyramids[1][level]);
                GP3.copyFrom(gaussianPyramids[2][level]);

                LP1.copyFrom(laplacianPyramids[0][level]);
                LP2.copyFrom(laplacianPyramids[1][level]);
                LP3.copyFrom(laplacianPyramids[2][level]);
            }

            // - - - - Script - - - - -
            scriptCollapse.set_GP1(GP1);
            scriptCollapse.set_GP2(GP2);
            scriptCollapse.set_GP3(GP3);
            scriptCollapse.set_LP1(LP1);
            scriptCollapse.set_LP2(LP2);
            scriptCollapse.set_LP3(LP3);
            scriptCollapse.forEach_multiplyBMP(outAlloc);

            bmpResultantPyramid[level] = Bitmap.createBitmap(width, height, laplacianPyramids[0][level].getConfig());
            outAlloc.copyTo(bmpResultantPyramid[level]);
        }

        GP1.destroy();
        GP2.destroy();
        GP3.destroy();
        LP1.destroy();
        LP2.destroy();
        LP3.destroy();
        outAlloc.destroy();

        return bmpResultantPyramid;
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
