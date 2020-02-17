#pragma version(1)
#pragma rs java_package_name(com.adrino.renderscript)

rs_allocation inAlloc;

float __attribute__((kernel)) saturate (uint32_t x, uint32_t y){
    float4 in = convert_float4(rsGetElementAt_uchar4(inAlloc, x, y)) / 255;
    float mu = (in.r + in.g + in.b ) / 3;
    float stdv = sqrt((pow((in.r - mu), 2) + pow((in.g - mu), 2) + pow((in.b - mu), 2)) / 3);
    return stdv;
}