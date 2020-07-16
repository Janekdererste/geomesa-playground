import SceneLayer from "@/SceneLayer";
import {BufferAttribute, BufferGeometry, Color, Mesh, Points, ShaderMaterial} from "three";
import {LinkTrip} from "@/API";
import TrajectoryMapper from "@/TrajectoryMapper";

const trajectoryVertexShader = require('./TrajectoryVertex.glsl').default
const trajectoryFragmentShader = require('./TrajectoryFragment.glsl').default

export const TRAJECTORY_LAYER = 'trajectory-layer'

export default class TrafficAnimationLayer extends SceneLayer {


    constructor() {
        super(TrafficAnimationLayer.init());
    }

    private static init() {

        const material = new ShaderMaterial({
            vertexShader: trajectoryVertexShader,
            fragmentShader: trajectoryFragmentShader,
            uniforms: {
                color: {value: new Color(0xff0000)},
                size: {value: 20 * window.devicePixelRatio},
                time: {value: 13 * 3600}
            },
            transparent: true,
        })
        const points = new Points(TrafficAnimationLayer.createTrajectoryBufferGeometry(), material)
        points.name = TRAJECTORY_LAYER
        points.position.z = -10
        points.frustumCulled = false
        return points
    }

    private static createTrajectoryBufferGeometry() {
        const geometry = new BufferGeometry()
        geometry.setAttribute('position', new BufferAttribute(new Float32Array([]), 3))
        geometry.setAttribute('toPosition', new BufferAttribute(new Float32Array([]), 3))
        geometry.setAttribute('fromTime', new BufferAttribute(new Float32Array([]), 1))
        geometry.setAttribute('toTime', new BufferAttribute(new Float32Array([]), 1))
        return geometry
    }

    updateTime(time: number) {

        const material = (this._sceneObject as Points).material as ShaderMaterial
        material.uniforms['time'].value = time
    }

    updateTrajectories(linkTrips: LinkTrip[]) {

        const mapper = new TrajectoryMapper(linkTrips)

        this.updateBufferAttribute('position', mapper.Vehicles.positions, 3)
        this.updateBufferAttribute('toPosition', mapper.Vehicles.toPositions, 3)
        this.updateBufferAttribute('fromTime', mapper.Vehicles.fromTimes, 1)
        this.updateBufferAttribute('toTime', mapper.Vehicles.toTimes, 1)
    }

    private updateBufferAttribute(attributeName: string, array: Float32Array, itemSize: number) {

        const mesh = this.sceneObject as Mesh
        const geometry = mesh.geometry as BufferGeometry
        geometry.setAttribute(attributeName, new BufferAttribute(array, itemSize))
    }

}