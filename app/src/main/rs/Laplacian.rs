#pragma version(1)
#pragma rs java_package_name(com.adrino.renderscript)

rs_allocation laplacianLowerLevel; // uchar4

uchar4 __attribute__((kernel)) laplacian(uchar4 in, int32_t x, int32_t y) {
    uchar4 result = rsGetElementAt_uchar4(laplacianLowerLevel, x, y) - in;
    float red = (float)result.r;
    float green = (float)result.g;
    float blue = (float)result.b;

    if(red < 0.f){
        result.r = -red;
    }
    if(green < 0.f){
        result.g = -green;
    }
    if(blue < 0.f){
        result.b = -blue;
    }
    return result;
}