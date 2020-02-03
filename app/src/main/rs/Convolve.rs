#pragma version(1)
#pragma rs java_package_name(com.adrino.renderscript)


float* conv_kernel;
rs_allocation gIn;

static uint32_t w;
static uint32_t h;

const uint8_t kernel_width = 3;
const uint8_t kernel_height = 3;

void root(const uchar4* in, uchar4* out,uint32_t x, uint32_t y)
{
    if(x < 1 || y < 1)
        return;
    if((x > w - 1) || (y > h - 1))
        return;
    uint8_t kx,ky;
    float4 temp  = 0;
    const uchar4* kin = in - (kernel_width / 2) - (kernel_height / 2) * (w);
    for(kx = 0; kx < kernel_width; kx++)
    {
        for(ky = 0; ky < kernel_height;ky++)
        {
            temp += rsUnpackColor8888(kin[kx + kernel_width * ky]) * conv_kernel[kx + kernel_width * ky];
        }
    }
    temp.a = 1.0f;
    *out = rsPackColorTo8888(temp);
}

void setup()
{
    w = rsAllocationGetDimX(gIn);
    h = rsAllocationGetDimY(gIn);
}