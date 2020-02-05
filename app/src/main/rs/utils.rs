#pragma version(1)
#pragma rs java_package_name(com.adrino.renderscript)

// ------------------------------------------------------------------------------------------------
// Convert uchar RBG to float intensity (0.0 - 1.0)
// ------------------------------------------------------------------------------------------------
float __attribute__((kernel)) calcGreyscaleIntensity(uchar4 in, uint32_t x, uint32_t y) {
    int color = 0;
    color += in.r;
    color += in.g;
    color += in.b;
    return color / 765.0;
}

// ------------------------------------------------------------------------------------------------
// Multiply a buffer with a constant
// ------------------------------------------------------------------------------------------------
float multiplyFactor;

float __attribute__((kernel)) multiply(float in, uint32_t x, uint32_t y) {
    return in * multiplyFactor;
}

// ------------------------------------------------------------------------------------------------
// Convert float intensity (0.0 - 1.0) to uchar RBG
// ------------------------------------------------------------------------------------------------
uchar4 __attribute__((kernel)) calcRgbaIntensity(float in, uint32_t x, uint32_t y) {
    return (int) fmax(0.0f, fmin(255.0f, in * 255.0f));
}