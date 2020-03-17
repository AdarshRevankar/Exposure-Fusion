package com.adrino.hdr.camera;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.adrino.hdr.R;
import com.adrino.hdr.corehdr.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CameraUtils {

    /**
     * +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -+
     * |                          Image Size                           |
     * +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -+
     */
    static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                  int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            return choices[0];
        }
    }

    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    /**
     * +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -+
     * |                          Error Dialog                         |
     * +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -+
     */
    public static class ErrorDialog extends DialogFragment {
        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.finish();
                        }
                    })
                    .create();
        }
    }


    /**
     * +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -+
     * |                       Wobble Check Dialog                     |
     * +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -+
     */
    public static class WobbleDialog extends DialogFragment {
        private static final String ARG_MESSAGE = "message";

        public static WobbleDialog newInstance(String message) {
            WobbleDialog dialog = new WobbleDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage("Kindly hold the device steady while capturing image")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            WobbleDialog.this.dismiss();
                        }
                    })
                    .create();
        }
    }


    /**
     * +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -+
     * |                        Success Dialog                         |
     * +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -+
     */
    public static class SuccessDialog extends DialogFragment {
        private static final String ARG_MESSAGE = "message";

        static SuccessDialog newInstance(String message) {
            SuccessDialog dialog = new SuccessDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage("Congrats !!! You have successfully captured all 3 images with different exposures")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SuccessDialog.this.dismiss();
                        }
                    })
                    .create();
        }
    }

    /**
     * +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -+
     * |                   Camera Permission Dialog                    |
     * +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -+
     */
    public static class ConfirmationDialog extends DialogFragment {
        private static final int REQUEST_CAMERA_PERMISSION = 100;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Fragment parent = getParentFragment();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.request_permission)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            parent.requestPermissions(new String[]{Manifest.permission.CAMERA},
                                    REQUEST_CAMERA_PERMISSION);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Activity activity = parent.getActivity();
                                    if (activity != null) {
                                        activity.finish();
                                    }
                                }
                            })
                    .create();
        }
    }


    /**
     * +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -+
     * |                        Bitmap Saver                           |
     * +- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -+
     */
}

class BitmapSaver {
    private static List<Bitmap> multiExposureImageList = null;

    BitmapSaver() {
        multiExposureImageList = new ArrayList<>(Constants.INPUT_IMAGE_SIZE);
    }

    public static void add(Bitmap toAdd) {
        if (multiExposureImageList.size() <= Constants.INPUT_IMAGE_SIZE)
            multiExposureImageList.add(toAdd);
        else
            throw new IndexOutOfBoundsException("List size should not exceed " + Constants.INPUT_IMAGE_SIZE);
    }

    // Getter
    public static List<Bitmap> multiExposureImageList() {
        return new ArrayList<>(multiExposureImageList);
    }

    void clear(){
        multiExposureImageList.clear();
        multiExposureImageList = null;
    }
}
