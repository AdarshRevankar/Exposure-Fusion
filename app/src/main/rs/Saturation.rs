#pragma version(1)
#pragma rs java_package_name(com.adrino.renderscript)

rs_allocation inAlloc;

uchar4 __attribute__((kernel)) saturate (uint32_t x, uint32_t y){

    uchar4 in = rsGetElementAt_uchar4(inAlloc, x, y);

    float u = (in.r + in.g + in.b ) / (255.0f * 3.0f);

    // Calculate S(x,y)
    float stdv = sqrt((pow((in.r / 255.0f - u), 2) + pow((in.g / 255.0f - u), 2) + pow((in.b / 255.0f - u), 2))/3.0f);

    uchar4 pixel;
    pixel.r = stdv * 255.0f;
    pixel.g = stdv * 255.0f;
    pixel.b = stdv * 255.0f;
    pixel.a = 255.0f;

    return pixel;
}