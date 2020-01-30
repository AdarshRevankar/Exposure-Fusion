#pragma version(1)
#pragma rs java_package_name(com.adrino.renderscript)

rs_allocation w1;
rs_allocation w2;
rs_allocation w3;

uchar4 __attribute__((kernel)) normalizeWeights(int32_t x, int32_t y){

    // Get the Pixels
    float inW1 = rsGetElementAt_uchar4(w1, x, y).r;
    float inW2 = rsGetElementAt_uchar4(w2, x, y).r;
    float inW3 = rsGetElementAt_uchar4(w3, x, y).r;

    // Calculate Sum
    float normalWeight = inW1 / (inW1 + inW2 + inW3);

    // Return
    uchar4 pixel;
    pixel.r = normalWeight;
    pixel.g = normalWeight;
    pixel.b = normalWeight;
    pixel.a = 255.0f;

    return pixel;

}