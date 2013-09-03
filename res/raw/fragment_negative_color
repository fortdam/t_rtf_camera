#extension GL_OES_EGL_image_external : require

precision mediump float;

varying vec2 vTextureCoord;
uniform samplerExternalOES sTexture;

void main() {
  vec4 color = texture2D(sTexture, vTextureCoord);
  gl_FragColor = vec4(1.0-color.r, 1.0-color.g, 1.0-color.b, 1.0);
}