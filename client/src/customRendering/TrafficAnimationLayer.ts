import SceneLayer from "@/customRendering/SceneLayer";
import {BufferAttribute, BufferGeometry, Color, Mesh, Points, ShaderMaterial} from "three";
import Api, {Coord, LinkTrip} from "@/API";

const trajectoryVertexShader = require('./TrajectoryVertex.glsl').default
const trajectoryFragmentShader = require('./TrajectoryFragment.glsl').default

export const TRAJECTORY_LAYER = 'trajectory-layer'

export default class TrafficAnimationLayer extends SceneLayer {

    private nextBucketChangeTime: number = 0
    private store: LinkTripStore

    constructor() {
        super(TrafficAnimationLayer.init());
        this.store = new LinkTripStore({} as Api, 0, 0)
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

        if (time > this.nextBucketChangeTime) {
            const bucket = this.store.getBucket(time)
            this.updateLinkTrips(bucket.Vehicles)
            this.nextBucketChangeTime = bucket.ToTime
        }
    }

    updateLinkTrips(vehicles: MovingPoints) {

        this.updateBufferAttribute('position', vehicles.positions, 3)
        this.updateBufferAttribute('toPosition', vehicles.toPositions, 3)
        this.updateBufferAttribute('fromTime', vehicles.fromTimes, 1)
        this.updateBufferAttribute('toTime', vehicles.toTimes, 1)
    }

    private updateBufferAttribute(attributeName: string, array: Float32Array, itemSize: number) {

        const mesh = this.sceneObject as Mesh
        const geometry = mesh.geometry as BufferGeometry
        geometry.setAttribute(attributeName, new BufferAttribute(array, itemSize))
    }
}

class LinkTripStore {

    private buckets: VehicleBucket[] = []
    private bucketSize = 3599 // hard code this for now, use hourly intervalls
    private endTime: number;
    private startTime: number;
    private api: Api

    constructor(api: Api, startTime: number, endTime: number) {
        this.startTime = startTime
        this.endTime = endTime
        this.api = api
        this.fetchData() // fire and forget
    }

    getBucket(time: number) {
        let index = this.getIndex(time)
        return this.buckets[index]
    }

    private async fetchData() {

        // first naive implementation: fetch buckets for hourly intervalls until endTime is reached
        // all blocks are fetched consecutively - no fast forwarding - this may be replaced with something
        // more elaborate
        let fromTime = this.startTime
        let toTime = this.startTime + this.bucketSize

        while (toTime <= this.endTime) {

            await this.fetchBucket(fromTime, toTime)
            fromTime = toTime + 1 // no overlap between buckets
            toTime = toTime + this.bucketSize
        }
    }

    private async fetchBucket(fromTime: number, toTime: number) {

        const result = await this.api.getTrajectories(fromTime, toTime)
        const bucket = new VehicleBucket(result, fromTime, toTime)
        this.buckets.push(bucket)
    }

    private getIndex(time: number) {
        return Math.floor((time - this.startTime) / this.bucketSize)
    }
}


interface MovingPoints {
    positions: Float32Array
    toPositions: Float32Array
    fromTimes: Float32Array
    toTimes: Float32Array
    // colors: Float32Array do this later
}

class VehicleBucket {

    private vehicles: MovingPoints
    private fromTime: number
    private toTime: number

    constructor(linkTrips: LinkTrip[], fromTime: number, toTime: number) {

        this.fromTime = fromTime
        this.toTime = toTime
        this.vehicles = this.createVehicles(linkTrips)
    }

    public get Vehicles() {
        return this.vehicles
    }

    public get ToTime() {
        return this.toTime
    }


    private static pushCoord(pushInto: number[], coord: Coord) {
        pushInto.push(coord.x)
        pushInto.push(coord.y)
        pushInto.push(0)
    }

    private createVehicles(linkTrips: LinkTrip[]) {

        const fromCoords: number[] = []
        const toCoords: number[] = []
        const fromTimes: number[] = []
        const toTimes: number[] = []

        linkTrips.forEach(linkTrip => {

            VehicleBucket.pushCoord(fromCoords, linkTrip.from)
            VehicleBucket.pushCoord(toCoords, linkTrip.to)
            fromTimes.push(linkTrip.fromTime)
            toTimes.push(linkTrip.toTime)
        })

        return {
            fromTimes: new Float32Array(fromTimes),
            toTimes: new Float32Array(toTimes),
            positions: new Float32Array(fromCoords),
            toPositions: new Float32Array(toCoords),
        }
    }
}

