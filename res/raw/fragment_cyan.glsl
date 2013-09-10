#extension GL_OES_EGL_image_external : require

precision mediump float;

varying vec2 vTextureCoord;
uniform samplerExternalOES sTexture;

const vec3 monoMultiplier = vec3(0.299, 0.587, 0.114);
const vec3 cyanFactor = vec3(0.8, 1.2, 1.2);

void main() {
  vec4 color = texture2D(sTexture, vTextureCoord);
  float monoColor = dot(color.rgb,monoMultiplier);
  gl_FragColor = vec4(clamp(vec3(monoColor, monoColor, monoColor)*cyanFactor, 0.0, 1.0), 1.0);
}