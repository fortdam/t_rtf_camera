#pragma version(1)
#pragma rs java_package_name(com.example.t_rtf_camera)

int mImageWidth;
int mImageHeight;
const uchar2 *gInPixels;
uchar4 *gRotatePixels;

const static float3 gMonoMult = {0.299f, 0.587f, 0.114f};

uchar4 __attribute__((kernel)) blackwhite(uchar4 in, uint x, uint y){	
	float4 f4 = rsUnpackColor8888(in);
	float3 mono = dot(f4.rgb, gMonoMult);
	return rsPackColorTo8888(mono);
}

const static float3 gSepiaToneMult = {1.2f, 1.0f, 0.8f};

uchar4 __attribute__((kernel)) sepiatone(uchar4 in, uint x, uint y){
	float4 f4 = rsUnpackColor8888(in);
	float3 mono = dot(f4.rgb, gMonoMult);
	float3 st = gSepiaToneMult*mono;
	st.r = min(1.0f, st.r);
	return rsPackColorTo8888(st);
}

uchar4 __attribute__((kernel)) revert(uchar4 in, uint x, uint y){
	uchar4 out;
	
	out.a = in.a;
	out.r = 0xff - in.r;
	out.g = 0xff - in.g;
	out.b = 0xff - in.b;
	
	return out;
}


void root(const int32_t *v_in, int32_t *v_out, const void *usrData, uint32_t x, uint32_t y) {
    int32_t row_index = *v_in;

    for (int i = mImageWidth-1; i >= 0; i--) {
        
        uchar2 inPixel = gInPixels[row_index*mImageWidth+i];
        uchar4 out;
        
        out.a = 0xff;
    	out.r = inPixel[1] & 0xf8;
	    out.g = (inPixel[1]&0x07)<<5 | (inPixel[0]&0xe0)>>3;
	    out.b = (inPixel[0] & 0x1f) << 3;
	    	    
	    int targetPos = i*mImageHeight + (mImageHeight-row_index-1); 
	    
	    gRotatePixels[targetPos] = out;
    }
}
