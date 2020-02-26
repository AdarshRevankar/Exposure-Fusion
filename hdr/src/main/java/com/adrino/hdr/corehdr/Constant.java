package com.adrino.hdr.corehdr;

import java.security.InvalidParameterException;

public class Constant {

    public static final int SCALE_THRESHOLD = 1200;
    /**
     * INPUT_IMAGE_SIZE - Exact Number of images required for HDR
     */
    static final int INPUT_IMAGE_SIZE = 3;
    /**
     * MEMORY BOOST - Toggle
     * if turned on, then the direct HDR computation is provided
     */
    public static boolean MEM_BOOST = false;

    /**
     * SELECTED_INDEX - Pointer
     * Points to the index of which Set of List<Bitmap> has to be given
     * Initially it points to the 1st List ( index = 0 )
     */
    private static int SELECTED_INDEX = 0;

    /*  - - - - - - - - - - S E T T E R S - - - - - - - - - - */
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

    /*  - - - - - - - - - - G E T T E R S - - - - - - - - - - */
    static int getSelectedIndex(){
        return SELECTED_INDEX;
    }
}
