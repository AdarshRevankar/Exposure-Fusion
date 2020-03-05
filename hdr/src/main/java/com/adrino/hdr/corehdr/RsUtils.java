package com.adrino.hdr.corehdr;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * RsUtils class is created for the purpose of utilities required for RenderScript processing
 * Contains methods which are frequently used.
 */
public class RsUtils {

    /**
     * create2d - static method
     * 2 Dimensional allocation along X and Y axis of type {@param ElementType} is done.
     * +---+---+
     * | E | E |
     * height +---+---+
     * | E | E |
     * +---+---+
     * width
     * NOTE: Where E - ElementType ( Eg: if ElementType = Element.UCHAR32_4, then it can have ARGB content
     * TODO: PLEASE DO NOT GIVE 'X' AXIS ALLOCATION <=1.
     * TODO: 1 < width and 1 <= height is valid
     *
     * @param rs          RenderScript instance
     * @param width       Width of Allocation
     * @param height      Height of Allocation
     * @param elementType Type of Information to be stored in Allocation
     * @return Allocation of Dim {@param width} x {@param height}
     */
    static Allocation create2d(RenderScript rs, int width, int height, Element elementType) {
        Type.Builder vectorBufferBuilder = new Type.Builder(rs, elementType);
        vectorBufferBuilder.setX(width);
        vectorBufferBuilder.setY(height);
        return Allocation.createTyped(rs, vectorBufferBuilder.create(), Allocation.USAGE_SCRIPT);
    }

    /**
     * Resize Image(s)
     * Converts a list of Bitmap (Images) to a specific dimension.
     * Maintains the <b>original ratio</b>. Means scales down to SCALE_THRESHOLD defined in {@link Constants} class.
     * <p>
     * TODO: Internally calls resizeBmp(inBmpImage);
     *
     * @param inBmpImageList Input List of different dimension(s)
     * @return Scaled Down Image(s), of having max dim as SCALED_THRESHOLD
     */
    public static List<Bitmap> resizeBmp(List<Bitmap> inBmpImageList) {
        List<Bitmap> outBmpImageList = new ArrayList<>(inBmpImageList.size());
        for (Bitmap inBmpImage : inBmpImageList) {
            outBmpImageList.add(
                    resizeBmp(inBmpImage)
            );
        }
        return outBmpImageList;
    }

    /**
     * Resize Image
     * Converts a Single Bitmap (image) of any dimension to, image having maximum dimension as
     * SCALE_THRESHOLD and yet maintains the original ratio of {@param inBmpImage}.
     *
     * <b>Logic</b>:
     * - Maintains the ratio
     * - Maximum dimension length is SCALE_THRESHOLD of {@link Constants}
     *
     * @param inBmpImage Input Bitmap Image
     * @return Scaled Image
     */
    static Bitmap resizeBmp(Bitmap inBmpImage) {
        int imgWidth = inBmpImage.getWidth();
        int imgHeight = inBmpImage.getHeight();

        int scaledWidth = imgHeight > imgWidth ? (imgWidth * Constants.SCALE_THRESHOLD) / imgHeight : Constants.SCALE_THRESHOLD;
        int scaledHeight = imgHeight > imgWidth ? Constants.SCALE_THRESHOLD : (imgHeight * Constants.SCALE_THRESHOLD) / imgWidth;
        return Bitmap.createScaledBitmap(inBmpImage, scaledWidth, scaledHeight, true);
    }

    /**
     * ErrorViewer - Static Method
     * For debugging purpose
     */
    static void ErrorViewer(Object where, String title, String subjet) {
        Log.e("EV", " ");
        Log.e("EV", " ");
        Log.e("EV", " + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
        Log.e("EV", " |  " + where.getClass() + " : " + title);
        Log.e("EV", " + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
        Log.e("EV", " |  " + subjet);
        Log.e("EV", " + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
        Log.e("EV", " ");
        Log.e("EV", " ");
    }

    static void ErrorViewer(Object where, String title, String subject, String remarks) {
        Log.e("EV", " ");
        Log.e("EV", " ");
        Log.e("EV", " + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
        Log.e("EV", " |  " + where.getClass() + ": " + title);
        Log.e("EV", " + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
        Log.e("EV", " |  " + subject);
        Log.e("EV", " + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
        Log.e("EV", " |  " + remarks);
        Log.e("EV", " + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
        Log.e("EV", " ");
        Log.e("EV", " ");

    }

    static void destroy2DAllocation(List<List<Allocation>> inAlloc) {
        for (List<Allocation> eachAllocList : inAlloc) {
            for (Allocation alloc : eachAllocList) {
                alloc.destroy();
            }
        }
    }
}


class Level {
    int width, height;

    Level(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
