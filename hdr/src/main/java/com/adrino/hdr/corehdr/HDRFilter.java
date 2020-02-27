package com.adrino.hdr.corehdr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.ScriptIntrinsicConvolve3x3;

import com.adrino.hdr.ScriptC_Collapse;
import com.adrino.hdr.ScriptC_Exposure;
import com.adrino.hdr.ScriptC_Gaussian;
import com.adrino.hdr.ScriptC_Laplacian;
import com.adrino.hdr.ScriptC_NormalizeWeights;
import com.adrino.hdr.ScriptC_Saturation;
import com.adrino.hdr.ScriptC_utils;


/**
 * Created by Adarsh Revankar
 * Date: 03/02/2019
 * <b>
 * Filter Class which has requried HDR Functions
 * Contains functions:
 * 1. Convolve
 * 2. Saturation
 * 3. Exposure
 * 4. Normalization
 * 5. Gaussian Pyramid
 * 6. Laplacian Pyramid
 * 5. Product Pyramid ( Resultant )
 * 8. Collapse
 * </b>
 */


class HDRFilter implements HDRManager.HDRProcessor {

    private final static String TAG = "HDRFilter";
    private int PYRAMID_LEVELS;
    private RenderScript renderScript;
    enum DATA_TYPE {FLOAT32, FLOAT32_4}
    private Element elementFloat4, elementFloat;
    private static List<Level> levelsMeta;
    private static Bitmap.Config config;
    private static int width, height;

    HDRFilter(Context context) {
        renderScript = RenderScript.create(context);    // Do not destroy
        elementFloat4 = Element.F32_4(renderScript);
        elementFloat = Element.F32(renderScript);
    }

    /**
     *  Setting Meta Data
     *  Which is essential to maintain the consistency in between the Methods
     */
    @Override
    public void setMeta(int imWidth, int imHeight, Bitmap.Config imConfig) {
        Constants.MEM_BOOST = false;

        // Set image Dim
        width = imWidth;
        height = imHeight;
        config = imConfig;

        // Calculate Pyramid Levels
        // PyrLevels = log(min(w,h))/log(2)
        PYRAMID_LEVELS = (int) (Math.log(Math.min(imWidth, imHeight)) / Math.log(2));
        RsUtils.ErrorViewer(this, "IMAGE DIMENTIONS", " W :" + width + " H :" + height);
        RsUtils.ErrorViewer(this, "NUMBER OF LEVELS", "" + PYRAMID_LEVELS);
    }


    /**
     *   +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
     *   |                           CONTRAST                           |
     *   +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
     * @refer  Image Kernels - http://setosa.io/ev/image-kernels/
     * @refer  Handling Edge cases - https://en.wikipedia.org/wiki/Kernel_(image_processing)
     *
     * Here the Edge(s) of the given Image is(are) identified.
     * Kernel is a Matrix which Convolve (defined below) over the image to get the specific feature.
     * Kernel used here is :
     *      Edge kernel - Identifies the Edge
     *      [ 0  1  0
     *        1 -4  1
     *        0  1  0 ]
     * Convolution : is the process of Scanning the image horizontally vertical to compute the dot
     * product and store the resultant in the corresponding pixel in the resultant image.
     *
     * Image Edges are extracted from the Input Image, i.e having Contrast information.
     *
     * TODO: Be Careful when destroying Allocation
     *
     * @param bmpImages     Input Image(s) list
     * @return              List of Allocation of float ( 1 Dim ) containing Contrast Information
     *                      Allocation will have FLOAT32 elements color in the range of 0...1.
     */
    @Override
    public List<Allocation> applyConvolution3x3Filter(List<Bitmap> bmpImages) {

        float[] filter = {0, 1, 0, 1, -4, 1, 0, 1, 0};
        ScriptIntrinsicConvolve3x3 scriptConvolve = ScriptIntrinsicConvolve3x3.create(renderScript, elementFloat);
        ScriptC_utils scriptUtils = new ScriptC_utils(renderScript);

        Allocation inAlloc, grayAlloc, outAlloc;
        List<Allocation> outAllocList = new ArrayList<>(Constants.INPUT_IMAGE_SIZE);

        for (Bitmap inImage : bmpImages) {
            inAlloc = Allocation.createFromBitmap(renderScript, inImage);
            grayAlloc = RsUtils.create2d(renderScript, width, height, elementFloat);
            outAlloc = RsUtils.create2d(renderScript, width, height, elementFloat);

            // Bitmap -> GrayScale Image
            scriptUtils.set_inGrayAlloc(inAlloc);
            scriptUtils.forEach_convertRGBAToGray(grayAlloc);

            // GrayScale Image -> Convoluted
            scriptConvolve.setInput(grayAlloc);
            scriptConvolve.setCoefficients(filter);
            scriptConvolve.forEach(outAlloc);

            // Add it to List
            outAllocList.add(outAlloc);

            // Destroy
            inAlloc.destroy();
            grayAlloc.destroy();
        }
        scriptConvolve.destroy();

        return outAllocList;
    }

    /**
     *   +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
     *   |                           SATURATION                         |
     *   +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
     * Here the variation in the RGB parameters is highlighted.
     * if RGB = 255 125 0, Having high variation between R G B Components, So it will be given more
     * value in the range of 0 ... 1.
     * But RGB = 255 255 255, Having 0 Deviation hence it will be given value 0;
     *
     * Calculation is done by:
     *      mean = ( R + G + B ) / 3
     *      S = sqrt( (R - mean)^2 + (G - mean)^2 + (B - mean)^2 ) / 3 )
     *
     * TODO: Be Careful when destroying Allocation
     *
     * @param bmpImages     Input Image(s) list
     * @return              List of Allocation of float ( 1 Dim ) containing Saturation Information
     */
    @Override
    public List<Allocation> applySaturationFilter(List<Bitmap> bmpImages) {
        ScriptC_Saturation scriptSaturation = new ScriptC_Saturation(renderScript);

        Allocation inAllocation, outAllocation;
        List<Allocation> outAllocList = new ArrayList<>(Constants.INPUT_IMAGE_SIZE);

        for (Bitmap inImage : bmpImages) {

            // Allocation
            inAllocation = Allocation.createFromBitmap(renderScript, inImage);
            outAllocation = RsUtils.create2d(renderScript, width, height, elementFloat);

            // Perform Saturation Script
            scriptSaturation.set_inAlloc(inAllocation);
            scriptSaturation.forEach_saturate(outAllocation);

            // Add to list
            outAllocList.add(outAllocation);

            // Destroy
            inAllocation.destroy();
        }
        scriptSaturation.destroy();
        return outAllocList;
    }

    /**
     *   +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
     *   |                         WELL EXPOSURE                        |
     *   +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
     * Here the exposure variation is observed and given value accordingly in the range of 0...1.
     *
     * Calculation is Done by: Gaussian function
     *      E = e^( ( R - 0.5 )^2 + ( G - 0.5 )^2 + ( B - 0.5 )^2 ) / ( - 2 * alpha^2 )
     *      Here alpha is taken to be : 0.2
     *
     * TODO: Be Careful when destroying Allocation
     *
     * @param bmpImages     Input Image(s) list
     * @return              List of Allocation of float ( 1 Dim ) containing Exposure Information
     */
    @Override
    public List<Allocation> applyExposureFilter(List<Bitmap> bmpImages) {

        ScriptC_Exposure scriptExposure = new ScriptC_Exposure(renderScript);

        Allocation inAllocation, outAllocation;
        List<Allocation> outAllocList = new ArrayList<>(Constants.INPUT_IMAGE_SIZE);

        for (Bitmap inImage : bmpImages) {

            // Allocate
            inAllocation = Allocation.createFromBitmap(renderScript, inImage);
            outAllocation = RsUtils.create2d(renderScript, width, height, elementFloat);

            // Perform Exposure
            scriptExposure.set_inAllocation(inAllocation);
            scriptExposure.forEach_expose(outAllocation);

            // Add to list
            outAllocList.add(outAllocation);

            // Destroy
            inAllocation.destroy();
        }
        scriptExposure.destroy();

        return outAllocList;
    }

    /**
     *  TODO: Normalization - Dyanmically for all the images
     *   +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
     *   |                         NORMALISATION                        |
     *   +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
     * Normalization allows to identify the pixels which has the best information to take from.
     * From Each exposed Image identifying segments that contribute most to the information of HDR.
     *
     * Normal Weighted Image is obtained by:
     *
     *      for each pixel (x,y):
     *          Wi(x,y) = Ci(x,y) * Si(x,y) * Ei(x,y)
     *          ~Wi(x,y) = Wi(x,y) / Summation_i(Wi(x,y))
     *
     * Where,
     *      Where, Ci, Si, Ei - Contrast, Saturation, Exposure of ith image (pixels)
     *      Wi - Weighted Pixel value
     *      ~Wi - Nomralised Weighted Pixel value
     *
     * TODO: Be Careful when destroying Allocation
     *
     * @param contrast          List of Allocation which has contrast computed
     * @param saturation        List of Allocation which has saturation computed
     * @param wellExposeness    List of Allocation which has exposure computed
     * @return                  List of Allocation of float ( 1 Dim ) Normally Weighted Information
     */
    @Override
    public List<Allocation> computeNormalWeighted(List<Allocation> contrast,
                                                  List<Allocation> saturation,
                                                  List<Allocation> wellExposeness) {

        ScriptC_NormalizeWeights scriptNorm = new ScriptC_NormalizeWeights(renderScript);

        // - - - - - Allocate - - - - - - - -
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

        // - - - - - Normalise - - - - - - - -
        scriptNorm.forEach_normalizeWeights(outAlloc1);

        // - - - - - Store - - - - - - - -
        List<Allocation> outAllocList = new ArrayList<>(Constants.INPUT_IMAGE_SIZE);
        outAllocList.add(outAlloc1);
        outAllocList.add(outAlloc2);
        outAllocList.add(outAlloc3);

        // MemBoost Clear the Allocations used & not required
        if (Constants.MEM_BOOST) {
            for (int i = 0; i < contrast.size(); i++) {
                contrast.get(i).destroy();
                saturation.get(i).destroy();
                wellExposeness.get(i).destroy();
            }
        }

        scriptNorm.destroy();

        return outAllocList;
    }

    // Overridden Method
    List<Allocation> computeNormalWeighted(List<Bitmap> bitmapList) {
        return computeNormalWeighted(applyConvolution3x3Filter(bitmapList), applySaturationFilter(bitmapList), applyExposureFilter(bitmapList));
    }

    /**
     *   +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
     *   |                      GAUSSIAN PYRAMID                        |
     *   +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
     *   Gaussian Pyramid is Collection of same image but of different resolution.
     *   It can have at max {PYRAMID_LEVELS} pyramid levels.
     *
     *   The Configuration is:
     *      G0 - Lowest Level
     *      Gl - Highest Level,     where l - Max number of level
     *
     *  +------------------------------------------------------------------------------------------------+
     *  | Gaussian Level        Pyramid Visualization       Resolution   Scaled to Original Apherance    |
     *  |------------------------------------------------------------------------------------------------|
     *  |    G4                        _                       50x50     Most Blur                       |
     *  |    G3                       ____                   100x100     More Blur                       |
     *  |    G2                     ________                 200x200     Little Blur                     |
     *  |    G1                  ________________            400x400     Approximately Original          |
     *  |    G0          ________________________________    800x800     Original                        |
     *  |                                                                                                |
     *  +------------------------------------------------------------------------------------------------+
     *
     *  Pyramids have to converted to Half of its current resolution.
     *
     *  Logic:
     *          currPyr = Original
     *          G[0] = Original
     *          for l: maxPyramidLevel - 1
     *          |--- currPyr = REDUCE(currPyr, 2)
     *          |--- G[l] = currPyr
     *
     *  This is how Gaussian Pyramid will be generated from 1 Image.
     *  Since current method takes N images as input, Hence it produces N Pyramids of L Layers.
     *
     * @param bmpImageList  Input Image(s) (bitmap) of difference exposure(s)
     * @return              List of N Pyramids, where each pyramid have L Levels of Allocation (F4)
     */
    @Override
    public List<List<Allocation>> generateGaussianPyramid(List<Bitmap> bmpImageList) {
        //   +- - - - - - - - - - - - - - - - - - - - - - - - - - -+
        //   |                GAUSSIAN PYRAMID                     |
        //   +- - - - - - - - - - - - - - - - - - - - - - - - - - -+
        ScriptC_Gaussian scriptGaussian = new ScriptC_Gaussian(renderScript);
        ScriptC_utils scriptUtils = new ScriptC_utils(renderScript);

        List<List<Allocation>> outGaussianAllocationList = new ArrayList<>(bmpImageList.size());

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

            // Initialise Level Meta
            if (levelsMeta == null) {
                // For first Image only, Construct the list
                levelsMeta = new ArrayList<>(PYRAMID_LEVELS);
                levelsMeta.add(new Level(width, height));
            }

            levelWidth = width;
            levelHeight = height;

            for (int level = 1; level < PYRAMID_LEVELS; level++) {

                // G1 = REDUCE(G0)
                int prevW = levelWidth;
                int prevH = levelHeight;

                // - - - - - - Get Dimension - - - -
                if (levelsMeta.size() != PYRAMID_LEVELS) {

                    // => G[i] = G[i+1] / 2 - Half as the size of the previous stage
                    levelWidth = levelWidth % 2 == 0 ? levelWidth / 2 : (levelWidth - 1) / 2;
                    levelHeight = levelHeight % 2 == 0 ? levelHeight / 2 : (levelHeight - 1) / 2;
                    levelsMeta.add(new Level(levelWidth, levelHeight));

                } else {

                    // Next time get the same dimension as earlier
                    levelWidth = levelsMeta.get(level).width;
                    levelHeight = levelsMeta.get(level).height;
                }


                Allocation midAlloc = RsUtils.create2d(renderScript, prevW, prevH, Element.F32_4(renderScript));

                // - - - -  - - - - - - REDUCE( G[ - - - - - - - - - -
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

    /**
     * Overloaded method for Gaussian Pyramid where,
     * Allocation can be given as input & that input is sent to the original Gaussian Pyramid function
     */
    @Override
    public List<List<Allocation>> generateGaussianPyramid(List<Allocation> floatAlloc, DATA_TYPE data_type) {
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        // RenderScript - Gaussian.rs ( Convolve ) Float Allocation
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        return generateGaussianPyramid(convertAllocationFxToBMP(floatAlloc, DATA_TYPE.FLOAT32));
    }


    /**
     *   +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -+
     *   |                      LAPLACIAN PYRAMID ( Color Info )         |
     *   +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -+
     *   Laplacian Pyramid is Collection of images similar to the Gaussian Pyramid, but
     *   laplacian pyramids are have layers which have different information.
     *   It can have at max {PYRAMID_LEVELS} pyramid levels.
     *
     *   Laplacian pyramid are constructed from difference between two adjacent Gaussian Pyramid levels.
     *   Which means it stores the Change in information from on layer to other layer. And hence it boosts color
     *
     *   The Configuration is:
     *      L0 - Lowest Level
     *      Ll - Highest Level,     where l - Max number of level
     *
     *  +---------------------------------------------------------------------------------------+
     *  | LaplacianLevel        Pyramid Visualization       Resolution   Computation            |
     *  |---------------------------------------------------------------------------------------|
     *  |    L4                        _                       50x50     L4 = G4                |
     *  |    L3                       ____                   100x100     L3 = G3 - EXPAND(G4)   |
     *  |    L2                     ________                 200x200     L2 = G2 - EXPAND(G3)   |
     *  |    L1                  ________________            400x400     L1 = G1 - EXPAND(G2)   |
     *  |    L0          ________________________________    800x800     L0 = G0 - EXPAND(G1)   |
     *  |                                                                                       |
     *  +---------------------------------------------------------------------------------------+
     *
     *  This is how Laplacian Pyramid will be generated from 1 Image.
     *  Since current method takes N images as input, Hence it produces N Pyramids of L Layers.
     *
     * @param bmpInMultiExposures  Input Image(s) (bitmap) of difference exposure(s)
     * @return                     List of N Pyramids, where each pyramid have L Levels of Allocation (F4)
     */
    @Override
    public List<List<Allocation>> generateLaplacianPyramids(List<Bitmap> bmpInMultiExposures) {
        // + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
        // |    Laplacian Pyramid - laplacian.rs ( Diff b/n Gaussian Pyr.)   |
        // + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
        ScriptC_Gaussian scriptGaussian = new ScriptC_Gaussian(renderScript);
        ScriptC_Laplacian scriptLaplacian = new ScriptC_Laplacian(renderScript);

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

                expandedAlloc.destroy();

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

    /**
     *  Resultant Pyramid Generation
     *
     *  Logic:
     *      R[0] = Summation_i(Gi[0] * Li[0])
     *      R[1] = Summation_i(Gi[1] * Li[1])
     *      R[2] = Summation_i(Gi[2] * Li[2])
     *      ...
     *      R[L] = Summation_i(Gi[L] * Li[L])
     */
    @Override
    public List<Allocation> generateResultant(List<List<Allocation>> gaussianPyramids, List<List<Allocation>> laplacianPyramids) {

        // Check, |Gi| == |Li|
        if (gaussianPyramids.size() != laplacianPyramids.size()) {
            Log.e(TAG, "generateResultant: Parameter Length not equal | PARAM_LENGTH_ERROR ");
        }

        // - - - - - - - - - - - - - - - - - - -
        //          RESULTANT GENERATOR
        // - - - - - - - - - - - - - - - - - - -
        ScriptC_Collapse scriptCollapse = new ScriptC_Collapse(renderScript);

        List<Allocation> resultantPyramid = new ArrayList<>(PYRAMID_LEVELS);

        // - - - - For Each level - - - - -
        for (int level = 0; level < PYRAMID_LEVELS; level++) {
            Allocation outAlloc = RsUtils.create2d(renderScript, levelsMeta.get(level).width, levelsMeta.get(level).height, elementFloat4);

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

        if (Constants.MEM_BOOST) {
            for (int i = 0; i < gaussianPyramids.size(); i++) {
                for (int j = 0; j < PYRAMID_LEVELS; j++) {
                    gaussianPyramids.get(i).get(j).destroy();
                    laplacianPyramids.get(i).get(j).destroy();
                }
            }
        }

        RsUtils.ErrorViewer(this, "RESULTANT PYRAMID", "FINISHED  - Length : " + resultantPyramid.size());
        return resultantPyramid;
    }

    /**
     * Collapse Resultant Pyramid
     *
     * Logic: Add All the pixels by Scaling to previous level
     *      HDR = R0 + EXPAND( R1 + EXPAND( R2 + ... EXPAND( Rl ) ... ))
     */
    @Override
    public List<Allocation> collapseResultant(List<Allocation> resultant) {
        int lowestLevel = PYRAMID_LEVELS - 1;
        ScriptC_Collapse scriptCollapse = new ScriptC_Collapse(renderScript);
        ScriptC_Gaussian scriptGaussian = new ScriptC_Gaussian(renderScript);
        List<Allocation> collapsedList = new ArrayList<>(PYRAMID_LEVELS);

        Allocation collapseAlloc = RsUtils.create2d(renderScript,
                levelsMeta.get(PYRAMID_LEVELS - 2).width,
                levelsMeta.get(PYRAMID_LEVELS - 2).height,
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


            if (level > 0) {
                int nextW = levelsMeta.get(level - 1).width;
                int nextH = levelsMeta.get(level - 1).height;

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

        if (Constants.MEM_BOOST) {
            for (int i = 0; i < resultant.size(); i++) {
                resultant.get(i).destroy();
            }
        }

        collapsedList.add(collapseAlloc);
        return collapsedList;
    }


    List<Bitmap> convertAllocationFxToBMP(List<Allocation> inAllocList, DATA_TYPE data_type) {
        ScriptC_utils scriptUtils = new ScriptC_utils(renderScript);

        Allocation outAlloc;
        Bitmap outBmp;
        List<Bitmap> outBmpList = new ArrayList<>(inAllocList.size());

        for (int i = 0; i < inAllocList.size(); i++) {
            // Allocate
            outBmp = Bitmap.createBitmap(width, height, config);
            outAlloc = Allocation.createFromBitmap(renderScript, outBmp);

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

    /**
     * Covert List of Allocation {@param inListAllocation} which are FLOAT32_4 (RGBA) to List of Bitmap images.
     */
    List<Bitmap> convertAllocationBMPDynamic(List<Allocation> inLstAllocation) {
        Allocation outAlloc;
        Bitmap outBmp;
        List<Bitmap> outBmpList = new ArrayList<>(inLstAllocation.size());
        ScriptC_utils scriptUtils = new ScriptC_utils(renderScript);

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
        scriptUtils.destroy();
        return outBmpList;
    }


}