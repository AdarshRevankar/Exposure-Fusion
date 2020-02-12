#pragma version(1)
#pragma rs java_package_name(com.adrino.renderscript)

int compressTargetWidth;
int compressTargetHeight;
rs_allocation compressSource;

// =======================================================================
//                      Compressing the pyramid levels
// =======================================================================
float4 __attribute__((kernel)) compressFloat4Step1(int32_t x, int32_t y) {
    float4 out = 0;
    if(x > 1 && x < (compressTargetWidth - 2)) {
        int xp = x * 2;
        out += rsGetElementAt_float4(compressSource, xp - 2, y) * 0.05f;
        out += rsGetElementAt_float4(compressSource, xp - 1, y) * 0.25f;
        out += rsGetElementAt_float4(compressSource, xp, y) * 0.4f;
        out += rsGetElementAt_float4(compressSource, xp + 1, y) * 0.25f;
        out += rsGetElementAt_float4(compressSource, xp + 2, y) * 0.05f;
    }
    return out;
}

float4 __attribute__((kernel)) compressFloat4Step2(int32_t x, int32_t y) {
    float4 out = 0;
    if(y > 1 && y < (compressTargetHeight - 2)) {
        int yp = y * 2;
        out += rsGetElementAt_float4(compressSource, x, yp - 2) * 0.05f;
        out += rsGetElementAt_float4(compressSource, x, yp - 1) * 0.25f;
        out += rsGetElementAt_float4(compressSource, x, yp) * 0.4f;
        out += rsGetElementAt_float4(compressSource, x, yp + 1) * 0.25f;
        out += rsGetElementAt_float4(compressSource, x, yp + 2) * 0.05f;
    }
    return out;
}

// ============================================================================================
// Expanding the pyramid levels
// ============================================================================================

int expandTargetWidth;
int expandTargetHeight;
rs_allocation expandSource; // float

// Step 1: expand the Y direction
float4 __attribute__((kernel)) expandFloat4Step1(int32_t x, int32_t y) {

    float4 out = 0;

    if(y > 1 && y < (expandTargetHeight - 2)) {
        int yp = y / 2;

        if(yp * 2 == y) {
            // Even number, we are in-line with the source
            out += rsGetElementAt_float4(expandSource, x, yp - 1) * 0.175f;
            out += rsGetElementAt_float4(expandSource, x, yp) * 0.65f;
            out += rsGetElementAt_float4(expandSource, x, yp + 1) * 0.175f;

        } else {
            // Odd number, we are in-between the source
            out += rsGetElementAt_float4(expandSource, x, yp) * 0.5f;
            out += rsGetElementAt_float4(expandSource, x, yp + 1) * 0.5f;
        }
    }
    return out;
}

// Step 2: expand the X direction
float4 __attribute__((kernel)) expandFloat4Step2(int32_t x, int32_t y) {

    float4 out = 0;

    if(x > 1 && x < (expandTargetWidth - 2)) {
        int xp = x / 2;

        if(xp * 2 == x) {
            // Even number, we are in-line with the source
            out += rsGetElementAt_float4(expandSource, xp - 1, y) * 0.175f;
            out += rsGetElementAt_float4(expandSource, xp, y) * 0.65f;
            out += rsGetElementAt_float4(expandSource, xp + 1, y) * 0.175f;

        } else {
            // Odd number, we are in-between the source
            out += rsGetElementAt_float4(expandSource, xp, y) * 0.5f;
            out += rsGetElementAt_float4(expandSource, xp + 1, y) * 0.5f;
        }
    }
    return out;
}

