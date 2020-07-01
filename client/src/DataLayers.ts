import {
    BufferAttribute,
    BufferGeometry,
    Color,
    LineBasicMaterial,
    LineSegments,
    Mesh,
    Points,
    Scene,
    ShaderMaterial
} from "three";
import Network from "@/Network";
import {Trajectory} from "@/API";
import TrajectoryMapper from "@/TrajectoryMapper";

const NETWORK_LAYER = 'network-layer'
const TRAJECTORY_LAYER = 'trajectory-layer'

const trajectoryVertexShader = require('./TrajectoryVertex.glsl').default
const trajectoryFragmentShader = require('./TrajectoryFragment.glsl').default

export default class DataLayers {

    private scene = new Scene()

    constructor() {
        this.initializeNetworkLayer()
        this.initializeTrajectoryLayer()
    }

    get Scene() {
        return this.scene
    }

    updateNetworkLayer(network: Network) {

        const networkLayer = this.scene.getObjectByName(NETWORK_LAYER) as Mesh
        const geometry = networkLayer.geometry as BufferGeometry
        const attribute = new BufferAttribute(network.Coords, 3);
        attribute.needsUpdate = true
        geometry.setAttribute('position', attribute)
    }

    private initializeNetworkLayer() {

        const attribute = new BufferAttribute(new Float32Array([0, 0, 0, 10000, 10000, 0]), 3)
        const geometry = new BufferGeometry()
        geometry.setAttribute('position', attribute)
        const material = new LineBasicMaterial({color: 0x0000ff});
        const mesh = new LineSegments(geometry, material);
        mesh.name = NETWORK_LAYER
        mesh.position.z = -11
        mesh.frustumCulled = false
        this.scene.add(mesh)
    }

    private static createTrajectoryBufferGeometry() {
        const geometry = new BufferGeometry()
        geometry.setAttribute('position', new BufferAttribute(new Float32Array([]), 3))
        geometry.setAttribute('toPosition', new BufferAttribute(new Float32Array([]), 3))
        geometry.setAttribute('fromTime', new BufferAttribute(new Float32Array([]), 1))
        geometry.setAttribute('toTime', new BufferAttribute(new Float32Array([]), 1))
        return geometry
    }

    updateTrajectories(trajectories: Trajectory[]) {

        const mapper = new TrajectoryMapper(trajectories)

        this.updateBufferAttribute(TRAJECTORY_LAYER, 'position', mapper.Vehicles.positions)
        this.updateBufferAttribute(TRAJECTORY_LAYER, 'toPosition', mapper.Vehicles.toPosition)
        this.updateBufferAttribute(TRAJECTORY_LAYER, 'fromTime', mapper.Vehicles.fromTimes)
        this.updateBufferAttribute(TRAJECTORY_LAYER, 'toTime', mapper.Vehicles.toTimes)
    }

    private initializeTrajectoryLayer() {

        const material = new ShaderMaterial({
            vertexShader: trajectoryVertexShader,
            fragmentShader: trajectoryFragmentShader,
            uniforms: {
                color: {value: new Color(0xff0000)},
                size: {value: 20 * window.devicePixelRatio},
                time: {value: 22158}
            },
            transparent: true,
        })
        const points = new Points(DataLayers.createTrajectoryBufferGeometry(), material)
        points.name = TRAJECTORY_LAYER
        points.position.z = -10
        points.frustumCulled = false
        this.scene.add(points)
    }

    private updateBufferAttribute(layerName: string, attributeName: string, array: Float32Array) {
        const layer = this.scene.getObjectByName(layerName) as Mesh
        const geometry = layer.geometry as BufferGeometry
        geometry.setAttribute(attributeName, new BufferAttribute(array, 3))
    }
}