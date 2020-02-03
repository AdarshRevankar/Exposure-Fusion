#pragma version(1)
#pragma rs java_package_name(com.adrino.renderscript)

static rs_allocation C1;
static rs_allocation S1;
static rs_allocation E1;

static rs_allocation C2;
static rs_allocation S2;
static rs_allocation E2;

static rs_allocation C3;
static rs_allocation S3;
static rs_allocation E3;

uchar4 *w2;
uchar4 *w3;



uchar4 normalizeWeights(int32_t x, int32_t y){
    float c1 = rsGetElementAt_char4(C1, x, y).r / 255.0f;
    float s1 = rsGetElementAt_char4(S1, x, y).r / 255.0f;
    float e1 = rsGetElementAt_char4(E1, x, y).r / 255.0f;

    float c2 = rsGetElementAt_char4(C2, x, y).r / 255.0f;
    float s2 = rsGetElementAt_char4(S2, x, y).r / 255.0f;
    float e2 = rsGetElementAt_char4(E2, x, y).r / 255.0f;

    float c3 = rsGetElementAt_char4(C3, x, y).r / 255.0f;
    float s3 = rsGetElementAt_char4(S3, x, y).r / 255.0f;
    float e3 = rsGetElementAt_char4(E3, x, y).r / 255.0f;


    // Get the Pixels
    float inW1 = c1 * s1 * e1;
    float inW2 = c2 * s2 * e2;
    float inW3 = c3 * s3 * e3;

    // Calculate Sum
    float nW1 = inW1 / (inW1 + inW2 + inW3);
    float nW2 = inW2 / (inW1 + inW2 + inW3);
    float nW3 = inW3 / (inW1 + inW2 + inW3);

    // Return
    uchar4 pixel;
    pixel.a = 255.0f;
    pixel.rgb = (char)nW2;
    *w2 = pixel;
    pixel.rgb = (char)nW3;
    *w3 = pixel;

    return pixel;

}