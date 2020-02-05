#pragma version(1)
#pragma rs java_package_name(com.adrino.renderscript)

int compressTargetWidth;
int compressTargetHeight;
rs_allocation compressSource;


// ============================================================================================
// Compressing the pyramid levels
// ============================================================================================

uchar4 __attribute__((kernel)) compressStep1(int32_t x, int32_t y) {

    float4 out = 0;
    if(x > 1 && x < (compressTargetWidth - 2)) {
        int xp = x * 2;
        out += convert_float4(rsGetElementAt_uchar4(compressSource, xp - 2, y)) * 0.05f;
        out += convert_float4(rsGetElementAt_uchar4(compressSource, xp - 1, y)) * 0.25f;
        out += convert_float4(rsGetElementAt_uchar4(compressSource, xp, y)) * 0.4f;
        out += convert_float4(rsGetElementAt_uchar4(compressSource, xp + 1, y)) * 0.25f;
        out += convert_float4(rsGetElementAt_uchar4(compressSource, xp + 2, y)) * 0.05f;
    }
    uchar4 outChar;
    outChar = convert_uchar4(out);
    outChar.a = 255.0f;
    return outChar;
}

uchar4 __attribute__((kernel)) compressStep2(int32_t x, int32_t y) {

    float4 out = 0;

    if(y > 1 && y < (compressTargetHeight - 2)) {
        int yp = y * 2;
        out += convert_float4(rsGetElementAt_uchar4(compressSource, x, yp - 2)) * 0.05f;
        out += convert_float4(rsGetElementAt_uchar4(compressSource, x, yp - 1)) * 0.25f;
        out += convert_float4(rsGetElementAt_uchar4(compressSource, x, yp)) * 0.4f;
        out += convert_float4(rsGetElementAt_uchar4(compressSource, x, yp + 1)) * 0.25f;
        out += convert_float4(rsGetElementAt_uchar4(compressSource, x, yp + 2)) * 0.05f;
    }

    uchar4 outChar;
    outChar = convert_uchar4(out);
    outChar.a = 255.0f;
    return outChar;
}

// ============================================================================================
// Expanding the pyramid levels
// ============================================================================================

int expandTargetWidth;
int expandTargetHeight;
rs_allocation expandSource; // float

// Step 1: expand the Y direction
uchar4 __attribute__((kernel)) expandStep1(int32_t x, int32_t y) {

    float4 out = 0;


    if(y > 1 && y < (expandTargetHeight - 2)) {
        int yp = y / 2;

        if(yp * 2 == y) {
            // Even number, we are in-line with the source
            out += convert_float4(rsGetElementAt_uchar4(expandSource, x, yp - 1)) * 0.175f;
            out += convert_float4(rsGetElementAt_uchar4(expandSource, x, yp)) * 0.65f;
            out += convert_float4(rsGetElementAt_uchar4(expandSource, x, yp + 1)) * 0.175f;

        } else {
            // Odd number, we are in-between the source
            out += convert_float4(rsGetElementAt_uchar4(expandSource, x, yp)) * 0.5f;
            out += convert_float4(rsGetElementAt_uchar4(expandSource, x, yp + 1)) * 0.5f;
        }
    }
    uchar4 outChar;
    outChar = convert_uchar4(out);
    outChar.a = 255.0f;
    return outChar;
}

// Step 2: expand the X direction
uchar4 __attribute__((kernel)) expandStep2(int32_t x, int32_t y) {

    float4 out = 0;

    if(x > 1 && x < (expandTargetWidth - 2)) {
        int xp = x / 2;

        if(xp * 2 == x) {
            // Even number, we are in-line with the source
            out += convert_float4(rsGetElementAt_uchar4(expandSource, xp - 1, y)) * 0.175f;
            out += convert_float4(rsGetElementAt_uchar4(expandSource, xp, y)) * 0.65f;
            out += convert_float4(rsGetElementAt_uchar4(expandSource, xp + 1, y)) * 0.175f;

        } else {
            // Odd number, we are in-between the source
            out += convert_float4(rsGetElementAt_uchar4(expandSource, xp, y)) * 0.5f;
            out += convert_float4(rsGetElementAt_uchar4(expandSource, xp + 1, y)) * 0.5f;
        }
    }

    uchar4 outChar;
    outChar = convert_uchar4(out);
    outChar.a = 255.0f;
    return outChar;
}

