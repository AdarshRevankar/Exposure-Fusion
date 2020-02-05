#pragma version(1)
#pragma rs java_package_name(com.adrino.renderscript)

int compressTargetWidth;
int compressTargetHeight;
rs_allocation compressSource;


// ============================================================================================
// Compressing the pyramid levels
// ============================================================================================

uchar4 __attribute__((kernel)) compressStep1(int32_t x, int32_t y) {

    float out = 0;
    if(x > 1 && x < (compressTargetWidth - 2)) {
        int xp = x * 2;
        out += rsGetElementAt_uchar4(compressSource, xp - 2, y).r * 0.05f;
        out += rsGetElementAt_uchar4(compressSource, xp - 1, y).r * 0.25f;
        out += rsGetElementAt_uchar4(compressSource, xp, y).r * 0.4f;
        out += rsGetElementAt_uchar4(compressSource, xp + 1, y).r * 0.25f;
        out += rsGetElementAt_uchar4(compressSource, xp + 2, y).r * 0.05f;
    }
    uchar4 outChar;
    outChar.rgb = out;
    outChar.a = 255.0f;
    return outChar;
}

uchar4 __attribute__((kernel)) compressStep2(int32_t x, int32_t y) {

    float out = 0;

    if(y > 1 && y < (compressTargetHeight - 2)) {
        int yp = y * 2;
        out += rsGetElementAt_uchar4(compressSource, x, yp - 2).r * 0.05f;
        out += rsGetElementAt_uchar4(compressSource, x, yp - 1).r * 0.25f;
        out += rsGetElementAt_uchar4(compressSource, x, yp).r * 0.4f;
        out += rsGetElementAt_uchar4(compressSource, x, yp + 1).r * 0.25f;
        out += rsGetElementAt_uchar4(compressSource, x, yp + 2).r * 0.05f;
    }

    uchar4 outChar;
    outChar.rgb = out;
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

    float out = 0;


    if(y > 1 && y < (expandTargetHeight - 2)) {
        int yp = y / 2;

        if(yp * 2 == y) {
            // Even number, we are in-line with the source
            out += rsGetElementAt_uchar4(expandSource, x, yp - 1).r * 0.175f;
            out += rsGetElementAt_uchar4(expandSource, x, yp).r * 0.65f;
            out += rsGetElementAt_uchar4(expandSource, x, yp + 1).r * 0.175f;

        } else {
            // Odd number, we are in-between the source
            out += rsGetElementAt_uchar4(expandSource, x, yp).r * 0.5f;
            out += rsGetElementAt_uchar4(expandSource, x, yp + 1).r * 0.5f;
        }
    }
    uchar4 outChar;
    outChar.rgb = out;
    outChar.a = 255.0f;
    return outChar;
}

// Step 2: expand the X direction
uchar4 __attribute__((kernel)) expandStep2(int32_t x, int32_t y) {

    float out = 0;

    if(x > 1 && x < (expandTargetWidth - 2)) {
        int xp = x / 2;

        if(xp * 2 == x) {
            // Even number, we are in-line with the source
            out += rsGetElementAt_uchar4(expandSource, xp - 1, y).r * 0.175f;
            out += rsGetElementAt_uchar4(expandSource, xp, y).r * 0.65f;
            out += rsGetElementAt_uchar4(expandSource, xp + 1, y).r * 0.175f;

        } else {
            // Odd number, we are in-between the source
            out += rsGetElementAt_uchar4(expandSource, xp, y).r * 0.5f;
            out += rsGetElementAt_uchar4(expandSource, xp + 1, y).r * 0.5f;
        }
    }

    uchar4 outChar;
    outChar.rgb = out;
    outChar.a = 255.0f;
    return outChar;
}

