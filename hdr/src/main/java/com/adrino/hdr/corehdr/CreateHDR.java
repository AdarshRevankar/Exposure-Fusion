package com.adrino.hdr.corehdr;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.renderscript.Allocation;

import java.util.List;

public class CreateHDR implements HDRManager.Presenter {

    private static final String TAG = "CreateHDR";
    private HDRFilter hdrFilter = null;
    private List<Allocation> inAllocList = null;

    /**
     * Actions
     * Enumeration used to specify what kind of computation has to be performed
     * Defination of each of fields:
     * <p>
     * HDR          - Get HDR image of given Image list of different EV's [ NOTE : List size has to be INPUT_IMAGE_SIZE ]
     * CONTRAST     - Get Contrast of List of Image(s) [ NOTE : Internally converted to Gray scale ]
     * EXPOSED      - Get Exposure of List of Image(s)
     * SATURATION   - Get Saturation of List of Image(s)
     * NORMAL       - Get Normal Weight of List of Image(s)
     * GAUSSIAN     - Get Gaussian Pyramid of List of Image(s) [ NOTE : SELECT_INDEX has to be specified ]
     * LAPLACIAN    - Get Laplacian Pyramid of List of Image(s) [ NOTE : SELECT_INDEX has to be specified ]
     * RESULTANT    - Get Resultant of List of Images [ NOTE : List size has to be INPUT_IMAGE_SIZE ]
     */
    public enum Actions {
        HDR,
        CONTRAST,
        EXPOSED,
        GAUSSIAN,
        LAPLACIAN,
        NORMAL,
        RESULTANT,
        SATURATION
    }

    /**
     *  Constructor
     *
     * @param context   : Application Context
     */
    public CreateHDR(Context context){
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
     * @return              List of Bitmap, after processing
     */
    @Override
    public List<Bitmap> perform(List<Bitmap> bmpImagesList, Actions action) {

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
                inAllocList = hdrFilter.generateGaussianPyramid(hdrFilter.computeNormalWeighted(bmpResizedImageList), HDRFilter.DATA_TYPE.FLOAT32).get(Constant.getSelectedIndex());
                break;
            case LAPLACIAN:
                isPyramid = true;
                inAllocList = hdrFilter.generateLaplacianPyramids(bmpResizedImageList).get(Constant.getSelectedIndex());
                break;
            case RESULTANT:
                isPyramid = true;
                inAllocList = hdrFilter.generateResultant(hdrFilter.generateGaussianPyramid(hdrFilter.computeNormalWeighted(bmpResizedImageList), HDRFilter.DATA_TYPE.FLOAT32), hdrFilter.generateLaplacianPyramids(bmpResizedImageList));
                break;
            case HDR:
                isPyramid = true;
                inAllocList = hdrFilter.collapseResultant(hdrFilter.generateResultant(hdrFilter.generateGaussianPyramid(hdrFilter.computeNormalWeighted(bmpResizedImageList), HDRFilter.DATA_TYPE.FLOAT32), hdrFilter.generateLaplacianPyramids(bmpResizedImageList)));
                break;
        }
        return isPyramid ? hdrFilter.convertAllocationBMPDynamic(inAllocList) : hdrFilter.convertAllocationFxToBMP(inAllocList, HDRFilter.DATA_TYPE.FLOAT32);
    }

    /**
     * Overloaded Method
     * For action == GAUSSIAN and LAPLACIAN, their perform method return List<List<Bitmap>>
     * To select one of the List in it we use, {@attrib SELECT_INDEX} to index it.
     * So, {@param selectedIndex} is used to set the {@attrib SELECT_INDEX}.
     */
    @Override
    public List<Bitmap> perform(List<Bitmap> bmpImagesList, Actions action, int selectedIndex) {
        Constant.setSelectedIndex(selectedIndex);

        // Notify User
        if (!(action == Actions.GAUSSIAN || action == Actions.LAPLACIAN)) {
            Log.e(TAG, "perform: RECOMMEND TO USE `perform(List<Bitmap>, Actions)` FOR Action." + action);
        }
        return perform(bmpImagesList, action);
    }

    /**
     * Destroy the temporary allocation list
     * IMPORTANT : Call this in onDestroy()
     */
    public void destroy() {
        if (inAllocList != null) {
            for (Allocation alloc :
                    inAllocList) {
                alloc.destroy();
            }
            inAllocList = null;
        }
    }
}