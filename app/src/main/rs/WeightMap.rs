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
    pixel.r = (c.r * s.r * e.r);
    pixel.g = (c.g * s.g * e.g);
    pixel.b = (c.b * s.b * e.b);
    pixel.a = 255.0f;
    return pixel;
}