package com.adrino.hdr.corehdr;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Script;
import androidx.renderscript.ScriptIntrinsicConvolve3x3;
import androidx.renderscript.ScriptIntrinsicResize;

import com.adrino.hdr.ScriptC_Collapse;
import com.adrino.hdr.ScriptC_Exposure;
import com.adrino.hdr.ScriptC_Laplacian;
import com.adrino.hdr.ScriptC_Saturation;
import com.adrino.hdr.ScriptC_Normalize;
import com.adrino.hdr.ScriptC_utils;

import java.util.ArrayList;
import java.util.List;


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
    private Element elementFloat4, elementFloat;
    private static List<Level> levelsMeta;
    private static Bitmap.Config config;
    private static int width, height;

    HDRFilter(Context context) {
        renderScript = RenderScript.create(context);   // Do not destroy
        elementFloat4 = Element.F32_4(renderScript);
        elementFloat = Element.F32(renderScript);
    }

    /**
     * Setting Meta Data
     * Which is essential to maintain the consistency in between the Methods
     */
    @Override
    public void setMeta(int imWidth, int imHeight, Bitmap.Config imConfig) {

        // Set image Dim
        width = imWidth;
        height = imHeight;
        config = imConfig;

        // Calculate Pyramid Levels
        // PyrLevels = log(min(w,h))/log(2)
        PYRAMID_LEVELS = (int) (Math.log(Math.min(imWidth, imHeight)) / Math.log(2));
        levelsMeta = createLevelMetaData(PYRAMID_LEVELS);
        RsUtils.ErrorViewer(this, "IMAGE DIMENTIONS", " W :" + width + " H :" + height);
        RsUtils.ErrorViewer(this, "NUMBER OF LEVELS", "" + PYRAMID_LEVELS);
    }

    private List<Level> createLevelMetaData(int maxPyrLevels) {
        List<Level> levels = new ArrayList<>();
        Level curr = new Level(width, height);
        levels.add(curr);
        for (int i = 1; i < maxPyrLevels; i++) {
            curr = new Level(curr.width / 2, curr.height / 2);
            levels.add(curr);
        }
        return levels;
    }


    /**
     * +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
     * |                           CONTRAST                           |
     * +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
     *
     * @param bmpImages Input Image(s) list
     * @return List of Allocation of float ( 1 Dim ) containing Contrast Information
     * Allocation will have FLOAT32 elements color in the range of 0...1.
     * @refer Image Kernels - http://setosa.io/ev/image-kernels/
     * @refer Handling Edge cases - https://en.wikipedia.org/wiki/Kernel_(image_processing)
     * <p>
     * Here the Edge(s) of the given Image is(are) identified.
     * Kernel is a Matrix which Convolve (defined below) over the image to get the specific feature.
     * Kernel used here is :
     * Edge kernel - Identifies the Edge
     * [ 0  1  0
     * 1 -4  1
     * 0  1  0 ]
     * Convolution : is the process of Scanning the image horizontally vertical to compute the dot
     * product and store the resultant in the corresponding pixel in the resultant image.
     * <p>
     * Image Edges are extracted from the Input Image, i.e having Contrast information.
     * <p>
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
        scriptUtils.destroy();
        return outAllocList;
    }

    /**
     * +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
     * |                           SATURATION                         |
     * +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
     * Here the variation in the RGB parameters is highlighted.
     * if RGB = 255 125 0, Having high variation between R G B Components, So it will be given more
     * value in the range of 0 ... 1.
     * But RGB = 255 255 255, Having 0 Deviation hence it will be given value 0;
     * <p>
     * Calculation is done by:
     * mean = ( R + G + B ) / 3
     * S = sqrt( (R - mean)^2 + (G - mean)^2 + (B - mean)^2 ) / 3 )
     * <p>
     *
     * @param bmpImages Input Image(s) list
     * @return List of Allocation of float ( 1 Dim ) containing Saturation Information
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
     * +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
     * |                         WELL EXPOSURE                        |
     * +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
     * Here the exposure variation is observed and given value accordingly in the range of 0...1.
     * <p>
     * Calculation is Done by: Gaussian function
     * E = e^( ( R - 0.5 )^2 + ( G - 0.5 )^2 + ( B - 0.5 )^2 ) / ( - 2 * alpha^2 )
     * Here alpha is taken to be : 0.2
     * <p>
     *
     * @param bmpImages Input Image(s) list
     * @return List of Allocation of float ( 1 Dim ) containing Exposure Information
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
     * +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
     * |                         NORMALISATION                        |
     * +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
     * Normalization allows to identify the pixels which has the best information to take from.
     * From Each exposed Image identifying segments that contribute most to the information of HDR.
     * <p>
     * Normal Weighted Image is obtained by:
     * <p>
     * for each pixel (x,y):
     * Wi(x,y) = Ci(x,y) * Si(x,y) * Ei(x,y)
     * ~Wi(x,y) = Wi(x,y) / Summation_i(Wi(x,y))
     * <p>
     * Where,
     * Where, Ci, Si, Ei - Contrast, Saturation, Exposure of ith image (pixels)
     * Wi - Weighted Pixel value
     * ~Wi - Nomralised Weighted Pixel value
     * <p>
     *
     * @param contrast       List of Allocation which has contrast computed
     * @param saturation     List of Allocation which has saturation computed
     * @param wellExposeness List of Allocation which has exposure computed
     * @return List of Allocation of float ( 1 Dim ) Normally Weighted Information
     */
    @Override
    public List<Allocation> computeNormalWeighted(List<Allocation> contrast,
                                                  List<Allocation> saturation,
                                                  List<Allocation> wellExposeness) {

        ScriptC_Normalize scriptNormalize = new ScriptC_Normalize(renderScript);

        // - - - - - Allocate - - - - - - - -
        List<Allocation> weightedAllocList = new ArrayList<>(Constants.INPUT_IMAGE_SIZE);
        List<Allocation> normWeightedAllocList = new ArrayList<>(Constants.INPUT_IMAGE_SIZE);
        scriptNormalize.set_N(Constants.INPUT_IMAGE_SIZE);

        // Calculate Weighted Sum
        for (int i = 0; i < Constants.INPUT_IMAGE_SIZE; i++) {
            Allocation outAlloc = RsUtils.create2d(renderScript, width, height, elementFloat);

            // Setting up values
            scriptNormalize.set_C(contrast.get(i));
            scriptNormalize.set_S(saturation.get(i));
            scriptNormalize.set_E(wellExposeness.get(i));
            scriptNormalize.set_N(i);
            if (i == 0) {
                scriptNormalize.set_sumW(RsUtils.create2d(renderScript, width, height, elementFloat));
            }

            // Weighted Values
            scriptNormalize.forEach_getWeighted(outAlloc);

            // Add to the list
            weightedAllocList.add(outAlloc);
        }

        // Calculate Normal Weighted
        for (int i = 0; i < Constants.INPUT_IMAGE_SIZE; i++) {
            Allocation outAlloc = RsUtils.create2d(renderScript, width, height, elementFloat);
            scriptNormalize.forEach_getNormalWeighted(weightedAllocList.get(i), outAlloc);
            normWeightedAllocList.add(outAlloc);
        }

        // Clear the Allocations used & not required
        RsUtils.destroy1DAllocation(weightedAllocList);
        RsUtils.destroy1DAllocation(contrast);
        RsUtils.destroy1DAllocation(saturation);
        RsUtils.destroy1DAllocation(wellExposeness);
        scriptNormalize.destroy();

        return normWeightedAllocList;
    }

    // Overridden Method
    List<Allocation> computeNormalWeighted(List<Bitmap> bitmapList) {
        return computeNormalWeighted(applyConvolution3x3Filter(bitmapList), applySaturationFilter(bitmapList), applyExposureFilter(bitmapList));
    }

    /**
     * +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
     * |                      GAUSSIAN PYRAMID                        |
     * +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
     * Gaussian Pyramid is Collection of same image but of different resolution.
     * It can have at max {PYRAMID_LEVELS} pyramid levels.
     * <p>
     * The Configuration is:
     * G0 - Lowest Level
     * Gl - Highest Level,     where l - Max number of level
     * <p>
     * +------------------------------------------------------------------------------------------------+
     * | Gaussian Level        Pyramid Visualization       Resolution   Scaled to Original Apherance    |
     * |------------------------------------------------------------------------------------------------|
     * |    G4                        _                       50x50     Most Blur                       |
     * |    G3                       ____                   100x100     More Blur                       |
     * |    G2                     ________                 200x200     Little Blur                     |
     * |    G1                  ________________            400x400     Approximately Original          |
     * |    G0          ________________________________    800x800     Original                        |
     * |                                                                                                |
     * +------------------------------------------------------------------------------------------------+
     * <p>
     * Pyramids have to converted to Half of its current resolution.
     * <p>
     * Logic:
     * currPyr = Original
     * G[0] = Original
     * for l: maxPyramidLevel - 1
     * |--- currPyr = REDUCE(currPyr, 2)
     * |--- G[l] = currPyr
     * <p>
     * This is how Gaussian Pyramid will be generated from 1 Image.
     * Since current method takes N images as input, Hence it produces N Pyramids of L Layers.
     *
     * @param bmpImageList Input Image(s) (bitmap) of difference exposure(s)
     * @return List of N Pyramids, where each pyramid have L Levels of Allocation (F4)
     */
    @Override
    public List<List<Allocation>> generateGaussianPyramid(List<Bitmap> bmpImageList) {
        //   +- - - - - - - - - - - - - - - - - - - - - - - - - - -+
        //   |                GAUSSIAN PYRAMID                     |
        //   +- - - - - - - - - - - - - - - - - - - - - - - - - - -+

        // Utilities
        ScriptC_utils scriptUtils = new ScriptC_utils(renderScript);

        // Intrinsic Rescale Class
        ScriptIntrinsicResize scriptIntrinsicResize = ScriptIntrinsicResize.create(renderScript);

        // List to hold Allocation output
        List<List<Allocation>> outGaussianAllocationList = new ArrayList<>(bmpImageList.size());

        // ========================== EXECUTION ==============================
        for (int i = 0; i < bmpImageList.size(); i++) {

            List<Allocation> outGaussLevelList = new ArrayList<>(PYRAMID_LEVELS);

            // - - - - - - Allocation - - - - - - -

            // G0 = Original Image
            Allocation G0 = convertBitmapToAllocation(bmpImageList.get(i), scriptUtils);

            Allocation inAlloc = RsUtils.create2d(renderScript, width, height, elementFloat4);
            inAlloc.copyFrom(G0);

            outGaussLevelList.add(G0);

            for (int level = 1; level < PYRAMID_LEVELS; level++) {

                // 1. Get current level dimension
                Level curr = levelsMeta.get(level);

                // 2. Down Sample : G1 = DOWNSCALE(G0)
                Allocation Gout = downScale(scriptIntrinsicResize, inAlloc, curr, null, true);

                // 3. Copy Input for recursive usage
                inAlloc = RsUtils.create2d(renderScript, curr.width, curr.height, Element.F32_4(renderScript));
                inAlloc.copyFrom(Gout);

                // 4. Store G1
                outGaussLevelList.add(Gout);
            }
            outGaussianAllocationList.add(outGaussLevelList);
        }
        scriptIntrinsicResize.destroy();
        scriptUtils.destroy();
//        RsUtils.ErrorViewer(this, "GAUSSIAN PYRAMID", "FINISHED");
        return outGaussianAllocationList;
    }

    /**
     * Overloaded method for Gaussian Pyramid where,
     * Allocation can be given as input & that input is sent to the original Gaussian Pyramid function
     */
    @Override
    public List<List<Allocation>> generateGaussianPyramid(List<Allocation> floatAlloc, Constants.DataType dataType) {
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        // RenderScript - Gaussian.rs ( Convolve ) Float Allocation
        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        return generateGaussianPyramid(convertAllocationListToBitmapList(floatAlloc, false, true));
    }


    /**
     * +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -+
     * |                      LAPLACIAN PYRAMID ( Color Info )         |
     * +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -+
     * Laplacian Pyramid is Collection of images similar to the Gaussian Pyramid, but
     * laplacian pyramids are have layers which have different information.
     * It can have at max {PYRAMID_LEVELS} pyramid levels.
     * <p>
     * Laplacian pyramid are constructed from difference between two adjacent Gaussian Pyramid levels.
     * Which means it stores the Change in information from on layer to other layer. And hence it boosts color
     * <p>
     * The Configuration is:
     * L0 - Lowest Level
     * Ll - Highest Level,     where l - Max number of level
     * <p>
     * +---------------------------------------------------------------------------------------+
     * | LaplacianLevel        Pyramid Visualization       Resolution   Computation            |
     * |---------------------------------------------------------------------------------------|
     * |    L4                        _                       50x50     L4 = G4                |
     * |    L3                       ____                   100x100     L3 = G3 - EXPAND(G4)   |
     * |    L2                     ________                 200x200     L2 = G2 - EXPAND(G3)   |
     * |    L1                  ________________            400x400     L1 = G1 - EXPAND(G2)   |
     * |    L0          ________________________________    800x800     L0 = G0 - EXPAND(G1)   |
     * |                                                                                       |
     * +---------------------------------------------------------------------------------------+
     * <p>
     * This is how Laplacian Pyramid will be generated from 1 Image.
     * Since current method takes N images as input, Hence it produces N Pyramids of L Layers.
     *
     * @param bmpInMultiExposures Input Image(s) (bitmap) of difference exposure(s)
     * @return List of N Pyramids, where each pyramid have L Levels of Allocation (F4)
     */
    @Override
    public List<List<Allocation>> generateLaplacianPyramids(List<Bitmap> bmpInMultiExposures) {
        // + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
        // |    Laplacian Pyramid - laplacian.rs ( Diff b/n Gaussian Pyr.)   |
        // + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +

        // Utilities
        ScriptC_utils scriptUtils = new ScriptC_utils(renderScript);

        // For Laplacian Transform
        ScriptC_Laplacian scriptLaplacian = new ScriptC_Laplacian(renderScript);

        // For Rescaling
        ScriptIntrinsicResize scriptIntrinsicResize = ScriptIntrinsicResize.create(renderScript);

        // - - - - Output Images - - - - - - - - - - - -
        List<List<Allocation>> laplacianPyramidList = new ArrayList<>(bmpInMultiExposures.size());

        // ========================== EXECUTION ==============================
        for (int i = 0; i < bmpInMultiExposures.size(); i++) {

            List<Allocation> outLap = new ArrayList<>(PYRAMID_LEVELS);

            // 1. Get Image Allocation & Convert to F4
            Allocation inAlloc = convertBitmapToAllocation(bmpInMultiExposures.get(i), scriptUtils);

            for (int lapLevel = 0; lapLevel < PYRAMID_LEVELS - 1; lapLevel++) {

                // Dimensions
                Level currDim = levelsMeta.get(lapLevel);
                Level smallDim = levelsMeta.get(lapLevel + 1);

                // 1.1 DOWN SCALE TO NEXT LEVEL I = downscale(J)
                Allocation downScaleOutAlloc = downScale(scriptIntrinsicResize, inAlloc, smallDim, null, false);

                // 1.2 UP SCALE TO CURRENT LEVEL
                Allocation upScaleOutAlloc = upScale(scriptIntrinsicResize, downScaleOutAlloc, currDim, null, false);

                // 1.3 SUBTRACT : J - upscale(I)
                Allocation lapOutputLevel = subtract(scriptLaplacian, inAlloc, upScaleOutAlloc, currDim, false, true);

                // 1.4 I = J
                inAlloc = downScaleOutAlloc;

                outLap.add(lapOutputLevel);
            }
            // L[N] = G[N] ie. Reduced Image
            outLap.add(inAlloc);

            laplacianPyramidList.add(outLap);
        }
        scriptLaplacian.destroy();
        scriptIntrinsicResize.destroy();
        scriptUtils.destroy();
        return laplacianPyramidList;
    }


    /**
     * Resultant Pyramid Generation
     * <p>
     * Logic:
     * R[0] = Summation_i(Gi[0] * Li[0])
     * R[1] = Summation_i(Gi[1] * Li[1])
     * R[2] = Summation_i(Gi[2] * Li[2])
     * ...
     * R[L] = Summation_i(Gi[L] * Li[L])
     */
    @Override
    public List<Allocation> generateResultant(List<List<Allocation>> gaussianPyramids, List<List<Allocation>> laplacianPyramids) {

        // Check, |Gi| == |Li|
        if (gaussianPyramids.size() != laplacianPyramids.size()) {
            Log.e(TAG, "generateResultant: Parameter Length not equal | PARAM_LENGTH_ERROR ");
            System.exit(401);
        }

        // + - - - - - - - - - - - - - - - - - - - - - - - - - - - -+
        // |                  Resultant Pyramid                     |
        // + - - - - - - - - - - - - - - - - - - - - - - - - - - - -+

        ScriptC_Collapse scriptCollapse = new ScriptC_Collapse(renderScript);

        List<Allocation> resultantPyramid = new ArrayList<>(PYRAMID_LEVELS);

        // FOR EACH LAYER PERFORM
        for (int level = 0; level < PYRAMID_LEVELS; level++) {

            // Get Dimension Info
            Level levelInfo = levelsMeta.get(level);
            Allocation outAlloc = RsUtils.create2d(renderScript, levelInfo.width, levelInfo.height, elementFloat4);

            scriptCollapse.set_GP1(gaussianPyramids.get(0).get(level));
            scriptCollapse.set_GP2(gaussianPyramids.get(1).get(level));
            scriptCollapse.set_GP3(gaussianPyramids.get(2).get(level));
            scriptCollapse.set_LP1(laplacianPyramids.get(0).get(level));
            scriptCollapse.set_LP2(laplacianPyramids.get(1).get(level));
            scriptCollapse.set_LP3(laplacianPyramids.get(2).get(level));
            scriptCollapse.forEach_multiplyBMP(outAlloc);

            resultantPyramid.add(outAlloc);
        }

        RsUtils.destroy2DAllocation(gaussianPyramids);
        RsUtils.destroy2DAllocation(laplacianPyramids);
//        RsUtils.ErrorViewer(this, "RESULTANT PYRAMID", "FINISHED  - Length : " + resultantPyramid.size());
        return resultantPyramid;
    }

    /**
     * Collapse Resultant Pyramid
     * <p>
     * Logic: Add All the pixels by Scaling to previous level
     * HDR = R0 + EXPAND( R1 + EXPAND( R2 + ... EXPAND( Rl ) ... ))
     */
    @Override
    public List<Allocation> collapseResultant(List<Allocation> resultant) {

        // + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
        // |                  Collapse Resultant Pyramid                     |
        // + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +

        int lowestLevel = PYRAMID_LEVELS - 1;

        // Utilities
        ScriptC_utils scriptUtils = new ScriptC_utils(renderScript);

        // Collapsing RenderScript
        ScriptC_Collapse scriptCollapse = new ScriptC_Collapse(renderScript);

        // Intrinsic Resize Script
        ScriptIntrinsicResize scriptIntrinsicResize = ScriptIntrinsicResize.create(renderScript);

        // HDR Output
        List<Allocation> collapsedList = new ArrayList<>(1);

        // Traversing the intermediate State
        Allocation collapseAlloc = RsUtils.create2d(renderScript,
                levelsMeta.get(lowestLevel - 1).width,
                levelsMeta.get(lowestLevel - 1).height,
                elementFloat4);

        // Set Allocation Values to Zero
        scriptUtils.forEach_setPixelToZero(collapseAlloc);

        // ========================== EXECUTION ==============================
        for (int level = lowestLevel - 1; level >= 0; level--) {
            Level curr = levelsMeta.get(level);

            // 1. UP SCALE 'PREV' IMAGE TO 'CURR' IMAGE
            Allocation expandedAlloc = upScale(scriptIntrinsicResize, resultant.get(level + 1), curr, null, false);

            // 2. ADD THAT TO 'collapseAlloc'
            scriptCollapse.set_collapseLevel(expandedAlloc);
            scriptCollapse.forEach_collapse(collapseAlloc, collapseAlloc);
            expandedAlloc.destroy();

            // 3. Expect R0 All other
            if (level > 0) {
                // 3.1 Get Info
                Level next = levelsMeta.get(level - 1);

                // 3.2 UPSCALE
                Allocation collExpandAlloc = upScale(scriptIntrinsicResize, collapseAlloc, next, null, true);

                // 3.3 Copy Content
                collapseAlloc = RsUtils.copy(renderScript, collExpandAlloc, next, true);
            }
        }
        collapsedList.add(collapseAlloc);
        RsUtils.destroy1DAllocation(resultant);
        scriptUtils.destroy();
        return collapsedList;
    }

    /**
     * ================================ BASIC FUNCTIONS ========================================
     */
    private Allocation downScale(ScriptIntrinsicResize scriptResize, @NonNull Allocation inAlloc, Level smallDimention, Script.LaunchOptions options, boolean destroy) {
        Allocation outAlloc = RsUtils.create2d(renderScript, smallDimention.width, smallDimention.height, inAlloc.getElement());

        scriptResize.setInput(inAlloc);
        if (options == null)
            scriptResize.forEach_bicubic(outAlloc);
        else
            scriptResize.forEach_bicubic(outAlloc, options);

        if (destroy) inAlloc.destroy();

        return outAlloc;
    }

    private Allocation upScale(ScriptIntrinsicResize scriptResize, @NonNull Allocation inAlloc, Level smallDimension, Script.LaunchOptions options, boolean destroy) {
        return downScale(scriptResize, inAlloc, smallDimension, options, destroy);
    }

    private Allocation subtract(ScriptC_Laplacian scriptLaplacian, @NonNull Allocation A, @NonNull Allocation B, Level currDim, boolean destroyA, boolean destroyB) {
        // Subtract
        // return A - B pixel to pixel subtraction is done
        Allocation lapOutAlloc = RsUtils.create2d(renderScript, currDim.width, currDim.height, elementFloat4);
        scriptLaplacian.set_laplacianLowerLevel(A);
        scriptLaplacian.forEach_laplacian(B, lapOutAlloc);
        if (destroyA) A.destroy();
        if (destroyB) B.destroy();
        return lapOutAlloc;
    }

    private Allocation subtract(ScriptC_Laplacian scriptLaplacian, @NonNull Allocation A, @NonNull Allocation B, Level currDim) {
        /* Intern Calls subtract() with other parameters */
        return subtract(scriptLaplacian, A, B, currDim, false, false);
    }

    /**
     * ================================ C O N V E R T O R S ========================================
     */

    // 1. List of Allocation --> List of Bitmap
    List<Bitmap> convertAllocationListToBitmapList(@NonNull List<Allocation> inLstAllocation, boolean isPyramid, boolean destroy) {

        // Container List for Bitmaps
        List<Bitmap> outBmpList = new ArrayList<>(inLstAllocation.size());

        for (int index = 0; index < inLstAllocation.size(); index++) {

            // Get output Dim
            // +--> if its a pyramid : Then Get Changing with, else get Original width information
            Level levelDim = isPyramid ? levelsMeta.get(index) : levelsMeta.get(0);

            // Convert to Bitmap and add
            outBmpList.add(convertAllocationToBitmap(inLstAllocation.get(index), levelDim, destroy));
        }
        return outBmpList;
    }

    // 2. Allocation ---> Bitmap
    private Bitmap convertAllocationToBitmap(@NonNull Allocation inAlloc, Level bmpInfo, boolean destroy) {
        ScriptC_utils scriptUtils = new ScriptC_utils(renderScript);
        Bitmap outBmp = Bitmap.createBitmap(bmpInfo.width, bmpInfo.height, config);
        Allocation outAlloc = Allocation.createFromBitmap(renderScript, outBmp);
        scriptUtils.set_inAlloc(inAlloc);

        if (inAlloc.getElement() == Element.F32(renderScript) ||
                inAlloc.getElement() == Element.F64(renderScript)) {
            scriptUtils.forEach_convertFtoU4(outAlloc);
        } else if (inAlloc.getElement() == Element.F32_4(renderScript) ||
                inAlloc.getElement() == Element.F64_4(renderScript)) {
            scriptUtils.forEach_convertF4toU4(outAlloc);
        } else if (inAlloc.getElement() == Element.U8_4(renderScript) ||
                inAlloc.getElement() == Element.U16_4(renderScript) ||
                inAlloc.getElement() == Element.U32_4(renderScript) ||
                inAlloc.getElement() == Element.U32_4(renderScript)) {
            inAlloc.copyTo(outBmp);
            if (destroy) inAlloc.destroy();
            outAlloc.destroy();
            return outBmp;
        } else
            throw new UnsupportedOperationException(
                    "Please check the supported Allocation data type, Supports only F32, F32_4"
            );

        if (destroy) inAlloc.destroy();
        outAlloc.copyTo(outBmp);
        scriptUtils.destroy();
        outAlloc.destroy();
        return outBmp;
    }

    // 3. Bitmap --> Allocation
    private Allocation convertBitmapToAllocation(Bitmap bitmap, ScriptC_utils utils) {
        Allocation allocation = RsUtils.create2d(renderScript, bitmap.getWidth(), bitmap.getHeight(), elementFloat4);
        utils.set_inAlloc(Allocation.createFromBitmap(renderScript, bitmap));
        utils.forEach_convertU4toF4(allocation);
        return allocation;
    }

    /**
     * DESTROY
     * TODO: CALL THIS METHOD
     */
    @Override
    public void destroy() {
        // Destroy RenderScript
        this.renderScript.destroy();

        // Initialise Meta to NULL
        levelsMeta = null;
    }
}