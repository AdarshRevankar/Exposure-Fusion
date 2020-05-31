package com.adrino.hdr.corehdr;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.renderscript.Allocation;

import java.util.List;

public class CreateHDR implements HDRManager.HDRClient {
    /**
     * CreateHDR is the class implements {@link HDRManager.HDRClient}, which provides the methods to perform HDR. it also allows developers
     * to inspect at intermediate stages of their result. CreateHDR performs the High-Dynamic-Range
     * (HDR) over the <b>Multiple Exposed</b> images using <b>Exposure Fusion</b> technique.
     * <p>
     * Method perform(List<Bitmap>, Actions) does take the request and returns the List of Bitmap
     * according to the {@link CreateHDR.Actions} Actions enumeration.
     * <p>
     * Way to obtain HDR Image from input of List<Bitmap> containing 3 Bitmap is :
     * Eg:
     * <p>
     * CreateHDR createHdr = new CreateHDR(getApplicationContext());
     * <p>
     * List<Bitmap> outBmpList = createHdr.perform( bmpInputList, CreateHDR.Actions.HDR );
     * <p>
     * Bitmap hdrOutput = outBmpList.get(0);
     * <p>
     * NOTE: Here each of the methods return variable number of output. Please make sure that index
     * is not out of Bound of List. ( Better way would be iterate using {@see Iterator} class.
     *
     * @see HDRFilter is the class where all the Methods are implemented.
     */

    private static final String TAG = "CreateHDR";
    private HDRFilter hdrFilter = null;

    /**
     * @see Actions
     * Enumeration used to specify what kind of computation has to be performed
     * Defination of each of fields:
     * <p>
     * +--------------------------------------------------------------------------------------------------------------------------------------------+
     * |   Action       No of Bitmaps Output    Description                                                                                         |
     * +--------------------------------------------------------------------------------------------------------------------------------------------+
     * |    HDR          1                       Get HDR image of given Image list of different EV's [ NOTE : List size has to be INPUT_IMAGE_SIZE ]|
     * |    CONTRAST     N                       Get Contrast of List of Image(s) [ NOTE : Internally converted to Gray scale ]                     |
     * |    EXPOSED      N                       Get Exposure of List of Image(s)                                                                   |
     * |    SATURATION   N                       Get Saturation of List of Image(s)                                                                 |
     * |    NORMAL       N                       Get Normal Weight of List of Image(s)                                                              |
     * |    GAUSSIAN     L                       Get Gaussian Pyramid of List of Image(s) [ NOTE : SELECT_INDEX has to be specified ]               |
     * |    LAPLACIAN    L                       Get Laplacian Pyramid of List of Image(s) [ NOTE : SELECT_INDEX has to be specified ]              |
     * |    RESULTANT    L                       Get Resultant of List of Images [ NOTE : List size has to be INPUT_IMAGE_SIZE ]                    |
     * +--------------------------------------------------------------------------------------------------------------------------------------------+
     * NOTE: Here, N - Length of bmpImagesList inputed
     * L - Pyramid Length computed dynamically (Depends on resolution)
     */
    public enum Actions {
        HDR,
        CONTRAST,
        EXPOSED,
        GAUSSIAN,
        LAPLACIAN,
        NORMAL,
        RESULTANT,
        SATURATION;


        @NonNull
        @Override
        public String toString() {
            return this.name();
        }
    }

    /**
     * Constructor
     *
     * @param context: Application Context
     */
    public CreateHDR(Context context) {
        if (hdrFilter == null) {
            hdrFilter = new HDRFilter(context);
        }
    }

    /**
     * Performing certain {@param action} over List<Bitmap>
     * Internally calls HDRFilter.actionMethod to return List<Bitmap>
     * NOTE: To perform HDR, pass the parameter Actions.HDR
     *
     * @param bmpImagesList Input List containing Bitmap of Different EV's
     * @param action        Process which has to be performed over the {@param bmpImageList}
     * @return List of Bitmap, after processing
     */
    @Override
    public List<Bitmap> perform(List<Bitmap> bmpImagesList, Actions action) {
        List<Allocation> inAllocList = null;

        // Resize the input bitmap
        List<Bitmap> bmpResizedImageList = RsUtils.resizeBmp(bmpImagesList);

        // Set MetaData For Processing
        hdrFilter.setMeta(
                bmpResizedImageList.get(0).getWidth(),
                bmpResizedImageList.get(0).getHeight(),
                bmpResizedImageList.get(0).getConfig()
        );

        boolean isPyramid = false;

        switch (action) {
            case CONTRAST:
                inAllocList = hdrFilter.applyConvolution3x3Filter(bmpResizedImageList);
                break;
            case SATURATION:
                inAllocList = hdrFilter.applySaturationFilter(bmpResizedImageList);
                break;
            case EXPOSED:
                inAllocList = hdrFilter.applyExposureFilter(bmpResizedImageList);
                break;
            case NORMAL:
                inAllocList = hdrFilter.computeNormalWeighted(bmpResizedImageList);
                break;
            case GAUSSIAN:
                isPyramid = true;
                inAllocList = hdrFilter.generateGaussianPyramid(hdrFilter.computeNormalWeighted(bmpResizedImageList), Constants.DataType.FLOAT32).get(Constants.getSelectedIndex());
                break;
            case LAPLACIAN:
                isPyramid = true;
                inAllocList = hdrFilter.generateLaplacianPyramids(bmpResizedImageList).get(Constants.getSelectedIndex());
                break;
            case RESULTANT:
                isPyramid = true;
                inAllocList = hdrFilter.generateResultant(hdrFilter.generateGaussianPyramid(hdrFilter.computeNormalWeighted(bmpResizedImageList), Constants.DataType.FLOAT32), hdrFilter.generateLaplacianPyramids(bmpResizedImageList));
                break;
            case HDR:
                isPyramid = true;
                inAllocList = hdrFilter.collapseResultant(hdrFilter.generateResultant(hdrFilter.generateGaussianPyramid(hdrFilter.computeNormalWeighted(bmpResizedImageList), Constants.DataType.FLOAT32), hdrFilter.generateLaplacianPyramids(bmpResizedImageList)));
                break;
        }
        return hdrFilter.convertAllocationListToBitmapList(inAllocList, isPyramid, true);
    }

    /**
     * Overloaded Method
     * For action == GAUSSIAN and LAPLACIAN, their perform method return List<List<Bitmap>>
     * To select one of the List in it we use, {@attrib SELECT_INDEX} to index it.
     * So, {@param selectedIndex} is used to set the {@attrib SELECT_INDEX}.
     */
    @Override
    public List<Bitmap> perform(List<Bitmap> bmpImagesList, Actions action, int selectedIndex) {
        Constants.setSelectedIndex(selectedIndex);

        // Notify User about Not Preferred method call
        if (!(action == Actions.GAUSSIAN || action == Actions.LAPLACIAN)) {
            Log.e(TAG, "perform: RECOMMEND TO USE `perform(List<Bitmap>, Actions)` FOR Action." + action);
        }
        return perform(bmpImagesList, action);
    }

    /**
     * Destroy the temporary allocation list
     * IMPORTANT : Call this in onDestroy()
     */
    @Override
    public void destroy() {
        // Destroy HDRFilter Contents
        hdrFilter.destroy();
    }
}