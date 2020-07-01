attribute vec3 toPosition;
attribute float fromTime;
attribute float toTime;

uniform float time;
uniform float size;
uniform vec3 color;

varying vec3 vColor;

vec3 interpolate(in vec3 from, in vec3 to, in float fromTime, in float toTime, in float time) {

    float fraction = (time - fromTime) / (toTime - fromTime);
    return (to - from) * fraction + from;
}

void main() {

    vColor = color;

    if (time < fromTime || toTime < time) {
        gl_Position = vec4(100.0, 0.0, 0.0, 0.0);// place outside clip space
    } else {
        gl_PointSize = size;
        vec3 interpolated = interpolate(position, toPosition, fromTime, toTime, time);
        gl_Position = projectionMatrix * modelViewMatrix * vec4(interpolated, 1.0);
    }
}
