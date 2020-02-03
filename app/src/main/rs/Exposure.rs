#pragma version(1)
#pragma rs java_package_name(com.adrino.renderscript)

rs_allocation inAllocation;
const float alpha = 0.2f;

uchar4 __attribute__((kernel))expose(uint32_t x, uint32_t y){
    // Get the current pixel
    uchar4 in = rsGetElementAt_uchar4(inAllocation, x, y);
    float4 inF = convert_float4(in) / 255.0f;

    // Gaussian Function
    float expontent = -(pow(inF.r - 0.5f,2)+pow(inF.g - 0.5f,2)+pow(inF.b - 0.5f,2)) / (2.f * pow(alpha, 2));
    float res = exp(expontent);

    // Return result
    uchar4 pixel;
    pixel.rgb = res * 255.0f;
    pixel.a = 255.0f;
    return pixel;
}
