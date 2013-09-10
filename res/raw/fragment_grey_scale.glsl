#extension GL_OES_EGL_image_external : require

precision mediump float;

varying vec2 vTextureCoord;
uniform samplerExternalOES sTexture;

const vec3 monoMultiplier = vec3(0.299, 0.587, 0.114);

void main() {
  vec4 color = texture2D(sTexture, vTextureCoord);
  float monoColor = dot(color.rgb,monoMultiplier);
  gl_FragColor = vec4(monoColor, monoColor, monoColor, 1.0);
}