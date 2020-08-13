import SceneLayer from "@/customRendering/SceneLayer";
import {BufferAttribute, BufferGeometry, Color, Mesh, Points, Raycaster, ShaderMaterial, Vector2} from "three";
import LinkTripStore from "@/store/LinkTripStore";
import Dispatcher from "@/store/Dispatcher";
import {SelectPerson, UnselectPerson} from "@/store/PlanStore";

const trajectoryVertexShader = require('./TrajectoryVertex.glsl').default
const trajectoryFragmentShader = require('./TrajectoryFragment.glsl').default

export const TRAJECTORY_LAYER = 'trajectory-layer'

export default class TrafficAnimationLayer extends SceneLayer {

    private store: LinkTripStore
    private raycaster = new Raycaster();
    private currentTime = 0

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

        this.currentTime = time
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

    intersect(coordinate: [number, number]) {

        if (!this.store.state.currentBucket || !this.store.state.currentBucket.vehicles) return

        // since there is interpolation involved, raycasting needs to be done manually, not by threejs raycaster
        const vehicles = this.store.state.currentBucket.vehicles
        const point = new Vector2(coordinate[0], coordinate[1])
        const intersections = []

        // tese are mutable anyway, so just re-use them
        const from = new Vector2()
        const to = new Vector2()
        const position = new Vector2()

        for (let i = 0; i < vehicles.ids.length; i++) {

            const startTime = vehicles.fromTimes[i]
            const endTime = vehicles.toTimes[i]

            if (startTime <= this.currentTime && this.currentTime <= endTime) {

                // could also define those outside for loop to avoid unnecessary objects being created
                from.fromArray(vehicles.positions, i * 3)
                to.fromArray(vehicles.toPositions, i * 3)

                const fraction = (this.currentTime - startTime) / (endTime - startTime)
                position.lerpVectors(from, to, fraction)

                // distance squared because the docs say it is more efficient - i guess now sqrt involved
                const distance = point.distanceToSquared(position)
                intersections.push({
                    distance: distance,
                    index: i
                })
            }
        }

        if (intersections.length > 0) {

            // sort by distance
            intersections.sort((i1, i2) => i1.distance - i2.distance)
            const closest = vehicles.ids[intersections[0].index]
            Dispatcher.dispatch(new SelectPerson(closest))
        } else {
            Dispatcher.dispatch(new UnselectPerson())
        }
    }

    private updateBufferAttribute(attributeName: string, array: Float32Array, itemSize: number) {

        const mesh = this.sceneObject as Mesh
        const geometry = mesh.geometry as BufferGeometry
        geometry.setAttribute(attributeName, new BufferAttribute(array, itemSize))
    }
}

