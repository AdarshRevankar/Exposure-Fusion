#pragma version(1)
#pragma rs java_package_name(com.adrino.renderscript)

rs_allocation inC;
rs_allocation inS;
rs_allocation inE;

uchar4 __attribute__((kernel)) computeWeight(int32_t x, int32_t y){
    uchar4 c = rsGetElementAt_uchar4(inC, x, y);
    uchar4 s = rsGetElementAt_uchar4(inS, x, y);
    uchar4 e = rsGetElementAt_uchar4(inE, x, y);

    uchar4 pixel;
    pixel.rgb = (float)e.r * (float)c.r * (float)s.r / (255.0f * 255.0f);
    pixel.a = 255.0f;
    return pixel;
}