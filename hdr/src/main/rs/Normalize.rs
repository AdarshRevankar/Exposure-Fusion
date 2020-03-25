#pragma version(1)
#pragma rs java_package_name(com.adrino.hdr)

int N;
rs_allocation sumW;
rs_allocation C;
rs_allocation S;
rs_allocation E;

float __attribute__((kernel)) getWeighted( int32_t x, int32_t y){
    // Computes the summation of the elements
    float c = rsGetElementAt_float(C,x,y);
    float s = rsGetElementAt_float(S,x,y);
    float e = rsGetElementAt_float(E,x,y);
    float currSumW = N == 0 ? 0:  rsGetElementAt_float(sumW, x, y);

    float addW = fabs(c)+fabs(s)+fabs(e);
    rsSetElementAt_float(sumW, currSumW + addW, x, y);
    return addW;
}

float __attribute__((kernel)) getNormalWeighted( float Wi, int32_t x, int32_t y){
    // Compute the allocation
    float sumWi = rsGetElementAt_float(sumW, x, y);
    float normWi = Wi / sumWi;
    return normWi;
}