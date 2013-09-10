#extension GL_OES_EGL_image_external : require

precision mediump float;

varying vec2 vTextureCoord;
uniform samplerExternalOES sTexture;

const float PI = 3.1415926535;
const float aperture = 180.0;
const float apertureHalf = 0.5 * aperture * (PI / 180.0);
const float maxFactor = sin(apertureHalf);


void main() {

  vec2 pos = 2.0 * vTextureCoord.st - 1.0;
  float l = length(pos);
  
  if (l > 1.0) {
    gl_FragColor = vec4(0, 0, 0, 1);
  } 
  else {
    float x = maxFactor * pos.x;
    float y = maxFactor * pos.y;
    float n = length(vec2(x, y));
    float z = sqrt(1.0 - n * n);
    float r = atan(n, z) / PI;
    float phi = atan(y, x);
    float u = r * cos(phi) + 0.5;
    float v = r * sin(phi) + 0.5;
  
    gl_FragColor = texture2D(sTexture, vec2(u, v));
  }
}