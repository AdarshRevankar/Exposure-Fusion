#pragma version(1)
#pragma rs java_package_name(com.adrino.renderscript)

rs_allocation inAllocation;
const float alpha = 0.2f;

float __attribute__((kernel))expose(uint32_t x, uint32_t y){

    // Get the current pixel
    float4 in = convert_float4(rsGetElementAt_uchar4(inAllocation, x, y)) / 255;

    // Gaussian Function
    float expontent = -(pow(in.r - 0.5f,2)+pow(in.g - 0.5f,2)+pow(in.b - 0.5f,2)) / (2.f * pow(alpha, 2));
    return exp(expontent);
}
