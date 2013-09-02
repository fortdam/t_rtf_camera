#extension GL_OES_EGL_image_external : require

precision mediump float;

varying vec2 vTextureCoord;
uniform samplerExternalOES sTexture;

void main() {
  //gl_FragColor = vec4(0, 1.0, 0, 1.0);
  gl_FragColor = texture2D(sTexture, vTextureCoord);
}