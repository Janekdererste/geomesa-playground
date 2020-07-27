varying vec3 vColor;

void main() {

    vec2 coord = gl_PointCoord - vec2(0.5);
    float radius = length(coord);

    // everything outside the circle doesn't need to be drawn
    if (radius > 0.5) discard;

    // draw 'color' inside and black ring outside. Umake a smooth transition
    vec3 colorToAssign = mix(vColor, vec3(0), smoothstep(0.35, 0.38, radius));
    //make a smooth transition on the outside of the circle as well
    float outsideSmoothing = smoothstep(0.5, 0.53, 1.0 - radius);

    gl_FragColor = vec4(colorToAssign, outsideSmoothing);
}