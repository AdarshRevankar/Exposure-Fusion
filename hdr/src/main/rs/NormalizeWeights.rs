#pragma version(1)
#pragma rs java_package_name(com.adrino.hdr)

rs_allocation out2;
rs_allocation out3;

rs_allocation C1;
rs_allocation S1;
rs_allocation E1;

rs_allocation C2;
rs_allocation S2;
rs_allocation E2;

rs_allocation C3;
rs_allocation S3;
rs_allocation E3;

// ============================================================================================
//                                      NORMALISE
// ============================================================================================
float __attribute__((kernel)) normalizeWeights(int32_t x, int32_t y){

    // Get C, S, E from all 3 images; Hence 3 x 3 = 9 variables
    float c1 = rsGetElementAt_float(C1, x, y);
    float s1 = rsGetElementAt_float(S1, x, y);
    float e1 = rsGetElementAt_float(E1, x, y);

    float c2 = rsGetElementAt_float(C2, x, y);
    float s2 = rsGetElementAt_float(S2, x, y);
    float e2 = rsGetElementAt_float(E2, x, y);

    float c3 = rsGetElementAt_float(C3, x, y);
    float s3 = rsGetElementAt_float(S3, x, y);
    float e3 = rsGetElementAt_float(E3, x, y);

    // Compute Weight
    float W1 = fabs(fabs(c1) * s1 * e1) + 0.000000000001f;
    float W2 = fabs(fabs(c2) * s2 * e2) + 0.000000000001f;
    float W3 = fabs(fabs(c3) * s3 * e3) + 0.000000000001f;

    // Normalize
    float weight1 = W1 / (W1 + W2 + W3);
    float weight2 = W2 / (W1 + W2 + W3);
    float weight3 = W3 / (W1 + W2 + W3);

    rsSetElementAt_float(out2, weight2, x, y);
    rsSetElementAt_float(out3, weight3, x, y);
    return weight1;
}