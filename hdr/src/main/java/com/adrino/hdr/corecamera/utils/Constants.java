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
    private static int incrementValue = 4;

    // Getter for exposure values
    public static int getIncrement(){
        return incrementValue;
    }
    public static int getLowEV() {
        return EXPOSURE_BRACKET[0];
    }

    public static int getMidEV() {
        return EXPOSURE_BRACKET[2];
    }

    public static int getHighEV() {
        return EXPOSURE_BRACKET[1];
    }

    // Setter
    public static void setLowEV(int value) {
        EXPOSURE_BRACKET[0] = value;
    }

    public static void setMidEV(int value) {
        EXPOSURE_BRACKET[2] = value;
    }

    public static void setHighEV(int value) {
        EXPOSURE_BRACKET[1] = value;
    }

    public static void setExposureBracket(int low, int mid, int high) {
        setLowEV(low);
        setHighEV(high);
        setMidEV(mid);
    }

    public enum CameraLens {
        LENS_FACING_FRONT,
        LENS_FACING_BACK
    }
}