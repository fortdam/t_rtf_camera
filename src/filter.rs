#pragma version(1)
#pragma rs java_package_name(com.example.t_rtf_camera)

int mImageWidth;
int mImageHeight;
const uchar2 *gInPixels;
uchar4 *gRotatePixels;


int __attribute__((kernel)) blackwhite(uchar4 in, uint x, uint y){	
	uchar greyScale = (in.r+in.g+in.b)/3;
	
	return (0xff000000 + (greyScale<<16) + (greyScale<<8) + greyScale);
}

int __attribute__((kernel)) sepiatone(uchar4 in, uint x, uint y){
	
	int cRed = min((int)(in.r*0.393f + in.g*0.769f + in.b*0.189f), 255);
	int cGreen = min((int)(in.r*0.349f + in.g*0.686f + in.b*0.168f), 255);
	int cBlue = min((int)(in.r*0.272f + in.g*0.534 + in.b*0.131f), 255);
	
	return (0xff000000 + (cRed<<16) + (cGreen<<8) + cBlue);
}

int __attribute__((kernel)) revert(uchar4 in, uint x, uint y){
	return (0xff000000 + ((255-in.r)<<16) + ((255-in.g)<<8) + (255-in.b));
}


void root(const int32_t *v_in, int32_t *v_out, const void *usrData, uint32_t x, uint32_t y) {
    int32_t row_index = *v_in;

    for (int i = mImageWidth-1; i >= 0; i--) {
        
        uchar2 inPixel = gInPixels[row_index*mImageWidth+i];
        uchar4 result;
        
        result.a = 0xff;
    	result.r = inPixel[1] & 0xf8;
	    result.g = (inPixel[1]&0x07)<<5 | (inPixel[0]&0xe0)>>3;
	    result.b = (inPixel[0] & 0x1f) << 3;
	    	    
	    int targetPos = i*mImageHeight + (mImageHeight-row_index-1); 
	    
	    gRotatePixels[targetPos] = result;//(0xff000000 + (red<<16) + (green<<8) + blue);
    }
}
