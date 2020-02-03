#pragma version(1)
#pragma rs java_package_name(com.adrino.renderscript)

rs_allocation nImageOut2;
rs_allocation nImageOut3;

rs_allocation C1;
rs_allocation S1;
rs_allocation E1;

rs_allocation C2;
rs_allocation S2;
rs_allocation E2;

rs_allocation C3;
rs_allocation S3;
rs_allocation E3;

uchar4 __attribute__((kernel)) normalizeWeights(int32_t x, int32_t y){

    // Get C, S, E from all 3 images; Hence 3 x 3 = 9 variables
    float c1 = rsGetElementAt_char4(C1, x, y).r / 255.0f;
    float s1 = rsGetElementAt_char4(S1, x, y).r / 255.0f;
    float e1 = rsGetElementAt_char4(E1, x, y).r / 255.0f;

    float c2 = rsGetElementAt_char4(C2, x, y).r / 255.0f;
    float s2 = rsGetElementAt_char4(S2, x, y).r / 255.0f;
    float e2 = rsGetElementAt_char4(E2, x, y).r / 255.0f;

    float c3 = rsGetElementAt_char4(C3, x, y).r / 255.0f;
    float s3 = rsGetElementAt_char4(S3, x, y).r / 255.0f;
    float e3 = rsGetElementAt_char4(E3, x, y).r / 255.0f;

    // Compute Weight
    float W1 = c1 * s1 * e1;
    float W2 = c2 * s2 * e2;
    float W3 = c3 * s3 * e3;

    // Normalize
    float weight1 = W1 / (W1 + W2 + W3);
    float weight2 = W2 / (W1 + W2 + W3);
    float weight3 = W3 / (W1 + W2 + W3);

    // Output
    // Image 2
    uchar4 pixel2;
    pixel2.rgb = weight2 * 255.0f;
    pixel2.a = 255.0f;
    rsSetElementAt_uchar4(nImageOut2, pixel2, x, y);

    // Image3
    uchar4 pixel3;
    pixel3.rgb = weight3 * 255.0f;
    pixel3.a = 255.0f;
    rsSetElementAt_uchar4(nImageOut3, pixel3, x, y);

    // Image1
    uchar4 pixel1;
    pixel1.rgb = weight1 * 255.0f;
    pixel1.a = 255.0f;
    return pixel1;
}