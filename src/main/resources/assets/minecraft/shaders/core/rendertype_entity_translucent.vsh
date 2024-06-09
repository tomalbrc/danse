#version 150

#moj_import <light.glsl>
#moj_import <fog.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D Sampler2;
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat3 IViewRotMat;
uniform vec3 Light0_Direction;
uniform vec3 Light1_Direction;
uniform int FogShape;

out float vertexDistance;
out vec4 vertexColor;
out vec4 lightMapColor;
out vec4 overlayColor;
out vec2 texCoord0;
out vec2 texCoord1;
out vec4 normal;
out float player;

#define SPACING 1024.0
#define MAXRANGE (0.5 * SPACING)

vec2 torsoT(int cube) {
    float v0 = UV0.y / (16. / 64.);
    if (v0 < 0.75)
      v0 *= 0.5;
    float v1 = (cube % 2 == 0 ? UV0.x : UV0.x - 0.5) / (32. / 64.);
    if (v1 < 12. / 32.)
      v1 /= 12. / 8.;
    else if (v1 > 20. / 32. && v1 < 28. / 32.)
      v1 /= 9. / 8.;
    if (cube % 2 == 0)
      return vec2(v1 * (24. / 64.) + 16. / 64., v0 * (16. / 64.) + 16. / 64.);
    else
      return vec2(v1 * (24. / 64.) + 16. / 64., v0 * (16. / 64.) + 32. / 64.);
}

vec2 leglT(int cube) {
    float v0 = UV0.y / (16. / 64.);
    if (v0 < 0.75)
      v0 *= 0.5;
    if (cube % 2 == 0)
      return vec2(UV0.x / (32. / 64.) * (16. / 64.) + 0. / 64.,
                        v0 * (16. / 64.) + 16. / 64.);
    else
      return vec2((UV0.x - 32. / 64.) / (32. / 64.) * (16. / 64.) + 0. / 64.,
               v0 * (16. / 64.) + 32. / 64.);
}

vec2 legrT(int cube) {
      float v0 = UV0.y / (16. / 64.);
      if (v0 < 0.75)
        v0 *= 0.5;
      if (cube % 2 == 0)
        return vec2(UV0.x / (32. / 64.) * (16. / 64.) + 16. / 64.,
                          v0 * (16. / 64.) + 48. / 64.);
      else
        return vec2((UV0.x - 32. / 64.) / (32. / 64.) * (16. / 64.) + 0. / 64.,
                 v0 * (16. / 64.) + 48. / 64.);
}

vec2 armrT(int cube) {
    int m0 = cube % 2;
    int m = m0 + 1;
    
    float v = UV0.y / 0.25;
    if (v < 0.75)
      v *= 0.5;
    float u = UV0.x * 2. - m0;
    u *= 16. / 64.;
    return vec2(u + 40. / 64., v * (16. / 64.) + m * 0.25);
}

vec2 armlT(int cube) {
    int m0 = cube % 2;
    int m = m0 + 1;
    
    float v = UV0.y / 0.25;
    if (v < 0.75)
      v *= 0.5;
    float u = UV0.x * 2. - m0;
    u *= 16. / 64.;
    return vec2(u + (32. + m0 * 16.) / 64., v * (16. / 64.) + 48. / 64.);
}


void main() {
  gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.);
  vertexColor = minecraft_mix_light(Light0_Direction, Light1_Direction, normalize(Normal), Color);
  lightMapColor = texelFetch(Sampler2, UV2 / 16, 0);
  overlayColor = texelFetch(Sampler1, UV1, 0);
  normal = ProjMat * ModelViewMat * vec4(Normal, 0.0);
  
  texCoord1 = vec2(0);
  vec2 modifiedUV = UV0;
  
  vec3 wpos = IViewRotMat * Position;
  int partId = -int((wpos.y - MAXRANGE) / SPACING);
  player = float(partId);
  
  if (textureSize(Sampler0, 0).x == 64 && textureSize(Sampler0, 0).y == 64 && ProjMat[2][3] != 0.0 && partId != 0) {
    int currentCube = ((gl_VertexID) / 24) % 14 + 2;
    currentCube = currentCube % 14;
    int i = (gl_VertexID) % 12;
    
    wpos.y += SPACING * partId;
    gl_Position = ProjMat * ModelViewMat * vec4(inverse(IViewRotMat) * wpos, 1.0);
    vertexDistance = fog_distance(wpos, FogShape);
    
    if (partId == 2) { // right arm
      modifiedUV = armrT(currentCube);
    } else if (partId == 3) { // left arm
      modifiedUV = armlT(currentCube);
    } else if (partId == 1) { // torso
      modifiedUV = torsoT(currentCube);
    } else if (partId == 4) { // left leg
      modifiedUV = leglT(currentCube);
    } else if (partId == 5) { // right leg
      modifiedUV = legrT(currentCube);
    }
    
    texCoord0 = modifiedUV;
  }
  else {
    player = 0;
    texCoord0 = UV0;
    texCoord1 = vec2(0);
    vertexDistance = fog_distance(IViewRotMat * Position, FogShape);
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
  }
}