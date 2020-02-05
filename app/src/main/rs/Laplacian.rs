#pragma version(1)
#pragma rs java_package_name(com.adrino.renderscript)

rs_allocation laplacianLowerLevel; // uchar4

uchar4 __attribute__((kernel)) laplacian(uchar4 in, int32_t x, int32_t y) {
    return in - rsGetElementAt_uchar4(laplacianLowerLevel, x, y);
}

// ============================================================================================
// Collapsing Laplacian pyramid levels
// ============================================================================================

rs_allocation collapseLevel; // float

float __attribute__((kernel)) collapse(float in, int32_t x, int32_t y) {
    return rsGetElementAt_float(collapseLevel, x, y) + in;
}