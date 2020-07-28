import SceneLayer from "@/customRendering/SceneLayer";
import {BufferAttribute, BufferGeometry, Color, Mesh, Points, ShaderMaterial} from "three";
import LinkTripStore from "@/store/LinkTripStore";

const trajectoryVertexShader = require('./TrajectoryVertex.glsl').default
const trajectoryFragmentShader = require('./TrajectoryFragment.glsl').default

export const TRAJECTORY_LAYER = 'trajectory-layer'

export default class TrafficAnimationLayer extends SceneLayer {

    private store: LinkTripStore

    constructor(store: LinkTripStore) {
        super(TrafficAnimationLayer.init());
        this.store = store
        store.register(() => this.updateLinkTrips())
    }

    private static init() {

        const material = new ShaderMaterial({
            vertexShader: trajectoryVertexShader,
            fragmentShader: trajectoryFragmentShader,
            uniforms: {
                color: {value: new Color(0xff0000)},
                size: {value: 10 * window.devicePixelRatio},
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

    updateLinkTrips() {

        if (this.store.state.currentBucket) {
            const vehicles = this.store.state.currentBucket?.vehicles
            this.updateBufferAttribute('position', vehicles.positions, 3)
            this.updateBufferAttribute('toPosition', vehicles.toPositions, 3)
            this.updateBufferAttribute('fromTime', vehicles.fromTimes, 1)
            this.updateBufferAttribute('toTime', vehicles.toTimes, 1)
        }
    }

    private updateBufferAttribute(attributeName: string, array: Float32Array, itemSize: number) {

        const mesh = this.sceneObject as Mesh
        const geometry = mesh.geometry as BufferGeometry
        geometry.setAttribute(attributeName, new BufferAttribute(array, itemSize))
    }
}

