package com.adrino.hdr.corehdr;

import java.security.InvalidParameterException;

public class Constants {
    /**
     * SCALE_THRESHOLD - Length of Maximum dimension of the image which is going to be Processed.
     * This specified the <b>Rescale Dimension</b> which makes the HDR process uniform.
     * TODO : Make sure, SCALE_THRESHOLD is not more than 4000 (Otherwise causes performance hit)
     */
    static int SCALE_THRESHOLD = 2000;

    /**
     * INPUT_IMAGE_SIZE - Exact Number of images required for HDR
     * TODO : Current Version, Supports only {@value INPUT_IMAGE_SIZE} 3
     * Please do not Modify
     */
    public static final int INPUT_IMAGE_SIZE = 3;

    /**
     * MEMORY BOOST - Toggle
     * if turned on, then the direct HDR computation is provided (memory clearance for intermediate stages)
     */
    public static boolean MEM_BOOST = false;

    /**
     * SELECTED_INDEX - Pointer
     * Points to the index of which Set of List<Bitmap> has to be given
     * Initially it points to the 1st List ( index = 0 )
     */
    private static int SELECTED_INDEX = 0;

    /**
     * - - - - - - - - - - - - - - - -
     *          SETTERS
     * - - - - - - - - - - - - - - - -
     */
    static void setScaleThreshold(int scaleThreshold) throws WrongValueError {
        SCALE_THRESHOLD = scaleThreshold > 4000 ? 4000 : scaleThreshold;
        if (scaleThreshold > 4000){
            throw new WrongValueError("Threshold value should be between 0 to 2000");
        }
    }

    static void setSelectedIndex(int index) {
        // Check if selected image is in range
        if (0 <= index && index < INPUT_IMAGE_SIZE) {
            // Set value
            SELECTED_INDEX = index;
        } else {
            // Throw Error
            throw new InvalidParameterException();
        }
    }

    /**- - - - - - - - - - - - - - - -
     *          GETTERS
     * - - - - - - - - - - - - - - - -
     */
    static int getScaleThreshold() {
        return SCALE_THRESHOLD;
    }

    static int getSelectedIndex() {
        return SELECTED_INDEX;
    }

    /**- - - - - - - - - - - - - - - -
     *          EXCEPTIONS
     * - - - - - - - - - - - - - - - -
     */
    private static class WrongValueError extends Throwable {
        WrongValueError(String s) {
            super(s);
        }
    }
}
