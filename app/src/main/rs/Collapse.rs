#pragma version(1)
#pragma rs java_package_name(com.adrino.renderscript)

// ============================================================================================
// Product Pyramid Formation = SUMi(LPi[l] * GPi[l])
// ============================================================================================
rs_allocation LP1; // float4
rs_allocation LP2;
rs_allocation LP3;

rs_allocation GP1; // float4s
rs_allocation GP2;
rs_allocation GP3;

float4 __attribute__((kernel)) multiplyBMP( int32_t x, int32_t y) {

    // Load Items
    float4 fLP1 = rsGetElementAt_float4(LP1, x, y);
    float4 fLP2 = rsGetElementAt_float4(LP2, x, y);
    float4 fLP3 = rsGetElementAt_float4(LP3, x, y);
    float4 fGP1 = rsGetElementAt_float4(GP1, x, y);
    float4 fGP2 = rsGetElementAt_float4(GP2, x, y);
    float4 fGP3 = rsGetElementAt_float4(GP3, x, y);

    // Compute
    float4 res = (fLP1 * fGP1 + fLP2 * fGP2 + fLP3 * fGP3);

    return res;
}

// ============================================================================================
// Collapsing Laplacian pyramid levels (GPi) = GP[i-1] + LPi
// ============================================================================================

rs_allocation collapseLevel; // uchar4

float4 __attribute__((kernel)) collapse(float4 in, int32_t x, int32_t y) {
    return rsGetElementAt_float4(collapseLevel, x, y) + in;
}