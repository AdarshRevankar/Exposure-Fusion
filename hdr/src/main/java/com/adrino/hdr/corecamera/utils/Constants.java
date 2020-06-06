package com.adrino.hdr.corecamera.utils;

public class Constants {

    // Please do change according to the requirement
    /**
     * EXPOSURE_BRACKET - Exposure Values (EV) for capturing from the camera
     * -ve means, Low Exposed
     * +ve means, Over Exposed
     * 0 means, Correct Exposed
     */
    public static int[] EXPOSURE_BRACKET = {-20, 10, 0};

    // Setter for exposure values
    public static void setExposureBracket(int low, int mid, int high){
        EXPOSURE_BRACKET[0] = low;
        EXPOSURE_BRACKET[1] = mid;
        EXPOSURE_BRACKET[2] = high;
    }
    public enum CameraLens{
        LENS_FACING_FRONT,
        LENS_FACING_BACK
    }
}