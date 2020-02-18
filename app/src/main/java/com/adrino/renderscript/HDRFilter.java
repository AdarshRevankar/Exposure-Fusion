package com.adrino.renderscript;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.ScriptIntrinsicConvolve3x3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.adrino.renderscript.Level;

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
    private static int PYRAMID_LEVELS;

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

    static List<Level> levelsMeta;

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
        width = imWidth;
        height = imHeight;
        config = imConfig;
        PYRAMID_LEVELS = (int) (Math.log(Math.min(imWidth, imHeight)) / Math.log(2));
        RsUtils.ErrorViewer(this, "IMAGE DIMENTIONS", " W :" + width + " H :" + height);
        RsUtils.ErrorViewer(this, "NUMBER OF LEVELS", "" + PYRAMID_LEVELS);
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

        List<List<Allocation>> outGaussianAllocationList = new ArrayList<>(3);

        for (int i = 0; i < bmpImageList.size(); i++) {
            List<Allocation> outGaussLevelList = new ArrayList<>(PYRAMID_LEVELS);

            // - - - - - - Allocation - - - - - - -
            // Convert U4 to F4
            Allocation startAlloc = Allocation.createFromBitmap(renderScript, bmpImageList.get(i));
            Allocation convertAlloc = RsUtils.create2d(renderScript, width, height, Element.F32_4(renderScript));
            Allocation inAlloc = RsUtils.create2d(renderScript, width, height, Element.F32_4(renderScript));

            scriptUtils.set_inAlloc(startAlloc);
            scriptUtils.forEach_convertU4toF4(convertAlloc);
            startAlloc.destroy();

            // - - - - - - Computation - - - - - -

            // G0 = Original Image
            inAlloc.copyFrom(convertAlloc);
            outGaussLevelList.add(convertAlloc);

            int levelWidth, levelHeight;
            if (levelsMeta == null) {
                levelsMeta = new ArrayList<>(PYRAMID_LEVELS);
                levelWidth = width;
                levelHeight = height;
                levelsMeta.add(new Level(width, height));
            } else {
                levelHeight = levelsMeta.get(0).height;
                levelWidth = levelsMeta.get(0).width;
            }

            for (int level = 1; level < PYRAMID_LEVELS; level++) {

                int prevW = levelWidth;
                int prevH = levelHeight;

                if (levelsMeta.size() != PYRAMID_LEVELS) {
                    levelWidth = levelWidth % 2 == 0 ? levelWidth / 2 : (levelWidth - 1) / 2;
                    levelHeight = levelHeight % 2 == 0 ? levelHeight / 2 : (levelHeight - 1) / 2;
                    levelsMeta.add(new Level(levelWidth, levelHeight));
                } else {
                    levelWidth = levelsMeta.get(level).width;
                    levelHeight = levelsMeta.get(level).height;
                }

                Allocation midAlloc = RsUtils.create2d(renderScript, prevW, prevH, Element.F32_4(renderScript));

                Log.e(TAG, "gaussianPyramid: W : " + levelWidth + " H : " + levelHeight);

                // REDUCE
                scriptGaussian.set_compressTargetWidth(levelWidth);
                scriptGaussian.set_compressTargetHeight(levelHeight);

                scriptGaussian.set_compressSource(inAlloc);
                scriptGaussian.forEach_compressFloat4Step1(midAlloc);

                inAlloc = RsUtils.create2d(renderScript, levelWidth, levelHeight, Element.F32_4(renderScript));

                scriptGaussian.set_compressSource(midAlloc);
                scriptGaussian.forEach_compressFloat4Step2(inAlloc);

                midAlloc.destroy();

                // Store Result
                outGaussLevelList.add(inAlloc);
            }
            outGaussianAllocationList.add(outGaussLevelList);
        }

        // 3. - - - Destroy & Return - - - -
        RsUtils.ErrorViewer(this, "GAUSSIAN PYRAMID", "FINISHED");
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
            int lapLevel = PYRAMID_LEVELS - 2;
            for (; lapLevel >= 0; lapLevel--) {

                int lapW = levelsMeta.get(lapLevel).width;
                int lapH = levelsMeta.get(lapLevel).height;

                int prevW = levelsMeta.get(lapLevel + 1).width;

                Allocation outAlloc = RsUtils.create2d(renderScript, prevW, lapH, elementFloat4);
                Allocation expandedAlloc = RsUtils.create2d(renderScript, lapW, lapH, elementFloat4);

                Log.e(TAG, "generateLaplacianPyramids: W : " + lapW + " H : " + lapH);

                scriptGaussian.set_expandTargetWidth(lapW);
                scriptGaussian.set_expandTargetHeight(lapH);

                scriptGaussian.set_expandSource(inGauss.get(lapLevel + 1));
                scriptGaussian.forEach_expandFloat4Step1(outAlloc);
                scriptGaussian.set_expandSource(outAlloc);
                scriptGaussian.forEach_expandFloat4Step2(expandedAlloc);
                outAlloc.destroy();

                Allocation lapAlloc = RsUtils.create2d(renderScript, lapW, lapH, elementFloat4);

                scriptLaplacian.set_laplacianLowerLevel(inGauss.get(lapLevel));
                scriptLaplacian.forEach_laplacian(expandedAlloc, lapAlloc);

                outLap.add(lapAlloc);
            }

            outLap.add(0, inGauss.get(PYRAMID_LEVELS - 1));
            Collections.reverse(outLap);

            // Attach to List
            laplacianPyramidList.add(outLap);
        }
        scriptLaplacian.destroy();

        RsUtils.ErrorViewer(this, "LAPLACIAN PYRAMID", "FINISHED");

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
            Allocation outAlloc = RsUtils.create2d(renderScript, levelsMeta.get(level).width, levelsMeta.get(level).height, elementFloat4);

            Log.e(TAG, "generateResultant: W : "+levelsMeta.get(level).width+" H : "+levelsMeta.get(level).height );
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

        RsUtils.ErrorViewer(this, "RESULTANT PYRAMID", "FINISHED  - Length : "+resultantPyramid.size());
        return resultantPyramid;
    }

    @Override
    public List<Allocation> collapseResultant(List<Allocation> resultant) {
        int lowestLevel = PYRAMID_LEVELS - 1;
        scriptCollapse = new ScriptC_Collapse(renderScript);
        scriptGaussian = new ScriptC_Gaussian(renderScript);
        List<Allocation> collapsedList = new ArrayList<>(PYRAMID_LEVELS);

        Allocation collapseAlloc = RsUtils.create2d(renderScript,
                                                    levelsMeta.get(PYRAMID_LEVELS-2).width,
                                                    levelsMeta.get(PYRAMID_LEVELS-2).height,
                                                    elementFloat4);

        for (int level = lowestLevel - 1; level >= 0; level--) {
            int lapW = levelsMeta.get(level).width;
            int lapH = levelsMeta.get(level).height;
            int prevW = levelsMeta.get(level + 1).width;

            Allocation outAlloc = RsUtils.create2d(renderScript, prevW, lapH, elementFloat4);
            Allocation expandedAlloc = RsUtils.create2d(renderScript, lapW, lapH, elementFloat4);

            scriptGaussian.set_expandTargetWidth(lapW);
            scriptGaussian.set_expandTargetHeight(lapH);

            scriptGaussian.set_expandSource(resultant.get(level + 1));
            scriptGaussian.forEach_expandFloat4Step1(outAlloc);
            scriptGaussian.set_expandSource(outAlloc);
            scriptGaussian.forEach_expandFloat4Step2(expandedAlloc);

            outAlloc.destroy();

            scriptCollapse.set_collapseLevel(expandedAlloc);
            scriptCollapse.forEach_collapse(collapseAlloc, collapseAlloc);

            expandedAlloc.destroy();


            if(level > 0){
                int nextW = levelsMeta.get(level-1).width;
                int nextH = levelsMeta.get(level-1).height;

                Allocation collExpandAlloc = RsUtils.create2d(renderScript, nextW, nextH, elementFloat4);

                scriptGaussian.set_expandTargetWidth(nextW);
                scriptGaussian.set_expandTargetHeight(nextH);

                scriptGaussian.set_expandSource(collapseAlloc);
                scriptGaussian.forEach_expandFloat4Step1(collExpandAlloc);

                collapseAlloc.destroy();
                collapseAlloc = RsUtils.create2d(renderScript, nextW, nextH, elementFloat4);

                scriptGaussian.set_expandSource(collExpandAlloc);
                scriptGaussian.forEach_expandFloat4Step2(collapseAlloc);
            }
        }
        collapsedList.add(collapseAlloc);
        return collapsedList;

//
//        Allocation inAllocation = RsUtils.create2d(renderScript, levelsMeta.get(0).width, levelsMeta.get(0).height, elementFloat4);
//        inAllocation.copyFrom(resultant.get(0));
//        Allocation outAllocation = null;
//
//        for (int level = PYRAMID_LEVELS; level < PYRAMID_LEVELS; level++) {
//            outAllocation = RsUtils.create2d(renderScript, levelsMeta.get(level-1).width, levelsMeta.get(level-1).height, elementFloat4);
//            Allocation expandedAlloc = RsUtils.create2d(renderScript, levelsMeta.get(level-1).width, levelsMeta.get(level-1).height, elementFloat4);
//
//            Log.e(TAG, "collapseResultant: Level - "+level );
//            // Expand inAlloc [ level - 1]
//            for (int j = level; j > 0; j--) {
//                scriptGaussian.set_expandTargetWidth(levelsMeta.get(j-1).width);
//                scriptGaussian.set_expandTargetHeight(levelsMeta.get(j-1).height);
//
//                Log.e(TAG, "collapseResultant: Expand Stage 0" );
//
//                scriptGaussian.set_expandSource(resultant.get(level));
//                scriptGaussian.forEach_expandFloat4Step1(outAllocation);
//
//                Log.e(TAG, "collapseResultant: Expand Stage 1" );
//
//                scriptGaussian.set_expandSource(outAllocation);
//                scriptGaussian.forEach_expandFloat4Step2(expandedAlloc);
//
//                //middleAllocation.copyFrom(expandedAlloc);
//            }
//
//            // Collapse [ level + (level -1 )]
//            scriptCollapse.set_collapseLevel(expandedAlloc);
//            scriptCollapse.forEach_collapse(inAllocation, outAllocation);
//
//            inAllocation.destroy();
//
//            inAllocation = RsUtils.create2d(renderScript, levelsMeta.get(level-1).width, levelsMeta.get(level-1).height, elementFloat4);
//            inAllocation.copyFrom(outAllocation);
//        }
//        collapsedList.add(outAllocation);
//        scriptGaussian.destroy();
//        scriptCollapse.destroy();

//        return collapsedList;
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

    public List<Bitmap> convertAllocationBMPDyamic(List<Allocation> inLstAllocation) {
        Allocation outAlloc;
        Bitmap outBmp;
        List<Bitmap> outBmpList = new ArrayList<>(inLstAllocation.size());
        scriptUtils = new ScriptC_utils(renderScript);

        int levelWidth = levelsMeta.get(0).width, levelHeight = levelsMeta.get(0).height;

        for (int i = 0; i < inLstAllocation.size(); i++) {

            outBmp = Bitmap.createBitmap(levelWidth, levelHeight, config);
            outAlloc = Allocation.createFromBitmap(renderScript, outBmp);

            // Perform
            scriptUtils.set_inAlloc(inLstAllocation.get(i));
            scriptUtils.forEach_convertF4toU4(outAlloc);

            outAlloc.copyTo(outBmp);
            outBmpList.add(outBmp);

            outAlloc.destroy();

            if (i < inLstAllocation.size() - 1) {
                levelHeight = levelsMeta.get(i + 1).height;
                levelWidth = levelsMeta.get(i + 1).width;
            }
        }
        return outBmpList;
    }


}