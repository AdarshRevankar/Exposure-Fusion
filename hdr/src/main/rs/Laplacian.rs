#pragma version(1)
#pragma rs java_package_name(com.adrino.hdr)

rs_allocation laplacianLowerLevel; // uchar4

float4 __attribute__((kernel)) laplacian(float4 in, int32_t x, int32_t y) {
    float4 prevG = rsGetElementAt_float4(laplacianLowerLevel, x, y);
    float4 res = prevG - in;
    return res;
}