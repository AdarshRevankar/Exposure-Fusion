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

    public enum CameraLens{
        LENS_FACING_FRONT,
        LENS_FACING_BACK
    }
}