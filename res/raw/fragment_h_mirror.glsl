#extension GL_OES_EGL_image_external : require

precision mediump float;

varying vec2 vTextureCoord;
uniform samplerExternalOES sTexture;

void main() {
  vec2 uv = vTextureCoord;
  if (vTextureCoord.y>0.5){
    uv.y = 1.0 - vTextureCoord.y;
  }
  gl_FragColor = texture2D(sTexture, uv);
}