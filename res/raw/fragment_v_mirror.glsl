#extension GL_OES_EGL_image_external : require

precision mediump float;

varying vec2 vTextureCoord;
uniform samplerExternalOES sTexture;

void main() {
  vec2 uv = vTextureCoord;
  if (vTextureCoord.x>0.5){
    uv.x = 1.0 - vTextureCoord.x;
  }
  gl_FragColor = texture2D(sTexture, uv);
}