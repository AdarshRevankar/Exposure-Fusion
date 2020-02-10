package com.adrino.renderscript;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Type;

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
public class HDRFilter implements HDRManager.Presenter {

    // Constants
    private final static String TAG = "HDRFilter";
    private static final int PYRAMID_LEVELS = 4;

    // Attributes
    private static RenderScript renderScript;
    private ScriptC_RGBtoGray scriptGray;
    private ScriptC_Convolve scriptConvolve;
    private ScriptC_Saturation scriptSaturation;
    private ScriptC_Exposure scriptExposure;
    private ScriptC_NormalizeWeights scriptNorm;
    private int width, height;
    private ScriptC_Laplacian scriptLaplacian;
    private ScriptC_Collapse scriptCollapse;
    private ScriptC_gaussian scriptGaussian;
    private static float[] laplacianKernel = {
            0.f, 1.f, 0.f,
            1.f, -4.f, 1.f,
            0.f, 1.f, 0.f
    };

    // Methods
    HDRFilter(Context context) {
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
        for (int i = 0; i < 3; i++) {
            bmpOut[i] = Bitmap.createBitmap(bitmapWidth, bitmapHeight, bmpImages[0].getConfig());
        }
        outAlloc1.copyTo(bmpOut[0]);
        outAlloc2.copyTo(bmpOut[1]);
        outAlloc3.copyTo(bmpOut[2]);

        // Destroy
        outAlloc1.destroy();
        outAlloc2.destroy();
        outAlloc3.destroy();
        c1.destroy();
        c2.destroy();
        c3.destroy();
        e1.destroy();
        e2.destroy();
        e3.destroy();
        s1.destroy();
        s2.destroy();
        s3.destroy();
        scriptNorm.destroy();
        return bmpOut;
    }

    /**
     * - - - - - - - - - - - - - - - - - - - -
     *      GAUSSIAN PYRAMID GENERATOR
     * - - - - - - - - - - - - - - - - - - - -
     * Given a single image ( GRAY ) Returns the Gaussian Pyramid ( 4 Layers )
     *
     * @param bmpSingleExp Single Exposure Image
     * @return 4 BmpImageArray == 4 Layers ( G0 == Bmp[0] and so on. )
     */
    @Override
    public Bitmap[] generateGaussianPyramid(Bitmap bmpSingleExp) {

        // - - - - - Init - - - - - - -
        scriptGaussian = new ScriptC_gaussian(renderScript);
        Bitmap[] outBmpGaussian = new Bitmap[4];
        width = bmpSingleExp.getWidth();
        height = bmpSingleExp.getHeight();
        for (int i = 1; i < 4; i++) {
            outBmpGaussian[i] = Bitmap.createBitmap(width, height, bmpSingleExp.getConfig());
        }

        // - - - - - - - - - - - - - - - - - - - -
        // RenderScript - gaussian.rs ( Convolve )
        // - - - - - - - - - - - - - - - - - - - -

        // 1. - - - Allocation - - - -
        Allocation inAlloc = Allocation.createFromBitmap(renderScript, bmpSingleExp);
        Allocation midAlloc = Allocation.createFromBitmap(renderScript, bmpSingleExp);
        Allocation outAlloc = Allocation.createFromBitmap(renderScript, bmpSingleExp);
        Allocation tempAlloc = Allocation.createFromBitmap(renderScript, bmpSingleExp);

        // 2. - - - Computation - - - -

        outBmpGaussian[0] = bmpSingleExp;

        for (int level = 1; level < PYRAMID_LEVELS; level++) {

            // REDUCE
            int compressDenom = (int) Math.pow(2, level);
            scriptGaussian.set_compressTargetWidth(width / compressDenom);
            scriptGaussian.set_compressTargetHeight(height / compressDenom);
            scriptGaussian.set_compressSource(inAlloc);
            scriptGaussian.forEach_compressStep1(midAlloc);
            scriptGaussian.set_compressSource(midAlloc);
            scriptGaussian.forEach_compressStep2(outAlloc);
            tempAlloc.copyFrom(outAlloc);
            inAlloc.copyFrom(outAlloc);

            // EXPAND
            for (int i = level - 1; i >= 0; i--) {
                int expandDenom = (int) Math.pow(2, i);
                scriptGaussian.set_expandTargetWidth(width * expandDenom);
                scriptGaussian.set_expandTargetHeight(height * expandDenom);
                scriptGaussian.set_expandSource(inAlloc);
                scriptGaussian.forEach_expandStep1(midAlloc);
                scriptGaussian.set_expandSource(midAlloc);
                scriptGaussian.forEach_expandStep2(outAlloc);
                inAlloc.copyFrom(outAlloc);
            }

            // Store Result
            outAlloc.copyTo(outBmpGaussian[level]);
            inAlloc.copyFrom(tempAlloc);
        }

        // 3. - - - Destroy & Return - - - -
        inAlloc.destroy();
        outAlloc.destroy();
        midAlloc.destroy();
        tempAlloc.destroy();

        return outBmpGaussian;
    }

    /**
     * - - - - - - - - - - - - - - - - - - -
     *      LAPLACIAN PYRAMID GENERATOR
     * - - - - - - - - - - - - - - - - - - - -
     * Given Multiple Images ( RGB ) Returns their Laplacian Pyramids ( N * 4 Layers )
     *
     * @param bmpInMultiExposures   Multiple RGB images
     * @return  N Laplacian Pyramids ( 4 Layers ) <= (Bitmap[][])
     */
    @Override
    public Bitmap[][] generateLaplacianPyramids(Bitmap[] bmpInMultiExposures) {

        // - - - - - - - - - - - - - - - - - - - -
        // Obtain - 3 Gaussian Pyramids ( 3 BMP )
        // - - - - - - - - - - - - - - - - - - - -
        Bitmap[][] bmpGaussianPyramids = new Bitmap[bmpInMultiExposures.length][PYRAMID_LEVELS];
        for (int i = 0; i < bmpInMultiExposures.length; i++) {

            // Gaussian Pyramids
            bmpGaussianPyramids[i] = generateGaussianPyramid(bmpInMultiExposures[i]);
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
        if(gaussianPyramids.length != laplacianPyramids.length){
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

    @Override
    public void destoryRenderScript() {
        renderScript.destroy();
        scriptGaussian.destroy();
        scriptCollapse.destroy();
    }

    // Create a 2D Array of certain w, h, element
    public Allocation create2d(int width, int height, Element elementType){
        Type.Builder vectorBufferBuilder = new Type.Builder(renderScript, elementType);
        vectorBufferBuilder.setX(width);
        vectorBufferBuilder.setY(height);
        return Allocation.createTyped(renderScript, vectorBufferBuilder.create(), Allocation.USAGE_SCRIPT);
    }
}

