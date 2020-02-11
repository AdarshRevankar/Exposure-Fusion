package com.adrino.renderscript;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Type;

public class RsUtils {
    public static Allocation create2d(RenderScript rs, int width, int height, Element elementType) {
        Type.Builder vectorBufferBuilder = new Type.Builder(rs, elementType);
        vectorBufferBuilder.setX(width);
        vectorBufferBuilder.setY(height);
        return Allocation.createTyped(rs, vectorBufferBuilder.create(), Allocation.USAGE_SCRIPT);
    }
}
