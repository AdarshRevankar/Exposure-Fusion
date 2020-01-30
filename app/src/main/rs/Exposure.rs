#pragma version(1)
#pragma rs java_package_name(com.adrino.renderscript)

rs_allocation inAllocation;
const float alpha = 0.2f;

uchar4 __attribute__((kernel))expose(uint32_t x, uint32_t y){
    // Get the current pixel
    uchar4 in = rsGetElementAt_uchar4(inAllocation, x, y);
    //rsDebug(" Exposure : In ", in);

    // Gaussian Function
    float expontent = -(pow(in.r - 0.5f,2)+pow(in.g - 0.5f,2)+pow(in.b - 0.5f,2)) / (2.f * pow(alpha, 2));
    float res = exp(expontent);

    // Return result
    uchar4 pixel;
    pixel.r = res;
    pixel.b = res;
    pixel.g = res;
    pixel.a = 1.f;

    rsDebug("Exposure",pixel);
    return pixel;

    //rsDebug(" Exposure : Out ", pixel);
}
