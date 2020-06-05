package com.adrino.renderscript.utils;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public class ImageItem {
    private Bitmap image;
    private String metaInfo;

    public ImageItem(Bitmap image, String metaInfo) {
        this.image = image;
        this.metaInfo = metaInfo;
    }

    public String getMetaInfo() {
        return metaInfo;
    }

    public Bitmap getImage() {
        return image;
    }

    public static ArrayList<ImageItem> createImageItemList(List<Bitmap> bmpImageList, List<String> StringMeta) {
        ArrayList<ImageItem> imageItemArrayList = new ArrayList<ImageItem>();

        if (bmpImageList.size() != StringMeta.size())
            throw new ArrayIndexOutOfBoundsException("Image List Size should be equal to the size of String Meta");
        else {
            for (int i = 0; i < bmpImageList.size(); i++) {
                imageItemArrayList.add(new ImageItem(bmpImageList.get(i), StringMeta.get(i)));
            }
        }
        return imageItemArrayList;
    }
}