#pragma version(1)
#pragma rs java_package_name(com.adrino.hdr)

//==============================================================================
//                            RGBA -> GRAY (FLOAT32)
//==============================================================================
rs_allocation inGrayAlloc;
const static float4 grayMultipliers = {0.299f, 0.587f, 0.114f, 1};

float __attribute__((kernel)) convertRGBAToGray(uint32_t x, uint32_t y) {
    float4 in = convert_float4(rsGetElementAt_uchar4(inGrayAlloc, x, y)) / 255;
    float pixelOut = dot(in, grayMultipliers);
    return pixelOut;
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

// ------------------------------------------------------------------------------------------------
// Float4toUchar4
// ------------------------------------------------------------------------------------------------
rs_allocation inAlloc;
uchar4 __attribute__((kernel)) convertF4toU4(uint32_t x, uint32_t y) {
    uchar4 pixel;
    float4 in = rsGetElementAt_float4(inAlloc, x, y);
    pixel.r = in.r * 255;
    pixel.g = in.g * 255;
    pixel.b = in.b * 255;
    pixel.a = 255;
    return pixel;
}

uchar4 __attribute__((kernel)) convertFtoU4(uint32_t x, uint32_t y) {
    uchar4 pixel;
    float in = rsGetElementAt_float(inAlloc, x, y);
    pixel = fabs(in) * 255;
    pixel.a = 255;
    return pixel;
}

float4 __attribute__((kernel)) convertU4toF4(uint32_t x, uint32_t y) {
    float4 out;
    float4 in = convert_float4(rsGetElementAt_uchar4(inAlloc, x, y)) / 255;
    out.r = in.r;
    out.g = in.g;
    out.b = in.b;
    out.a = 1;
    return out;
}

float4 __attribute__((kernel)) convertFtoF4(uint32_t x, uint32_t y){
    float4 out = rsGetElementAt_float(inAlloc, x, y);
    out.a = 1;
    return out;
}