import Api, {BucketReceivedAction, Coord, LinkTrip, SetInfoReceivedAction} from "@/API";
import Store from "@/store/Store";
import {Action} from "@/store/Dispatcher";
import {AdvanceTimeAction} from "@/store/PlaybackStore";
import ConfigurationStore from "@/store/ConfigurationStore";

export interface MovingPoints {
    positions: Float32Array
    toPositions: Float32Array
    fromTimes: Float32Array
    toTimes: Float32Array
    // colors: Float32Array do this later
}

export class VehicleBucket {

    private _fromTime: number

    constructor(linkTrips: LinkTrip[], fromTime: number, toTime: number) {

        this._fromTime = fromTime
        this._toTime = toTime
        this._vehicles = this.createVehicles(linkTrips)
    }

    private _vehicles: MovingPoints

    public get vehicles() {
        return this._vehicles
    }

    private _toTime: number

    public get toTime() {
        return this._toTime
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

export interface LinkTripState {
    buckets: VehicleBucket[],
    currentBucket: VehicleBucket | undefined
}

export default class LinkTripStore extends Store<LinkTripState> {

    private api: Api
    private bucketSize = 3599 // hard code this for now, use hourly intervals
    private configStore: ConfigurationStore;

    constructor(api: Api, configStore: ConfigurationStore) {
        super()
        this.api = api
        this.configStore = configStore
    }

    protected getInitialState(): LinkTripState {
        return {buckets: [], currentBucket: undefined};
    }

    protected reduce(state: LinkTripState, action: Action): LinkTripState {

        if (action instanceof AdvanceTimeAction) {

            const index = this.getIndex(action.time)
            if (this.state.buckets[index] !== this.state.currentBucket) {
                return Object.assign({}, state, {currentBucket: this.state.buckets[index]})
            }
        } else if (action instanceof BucketReceivedAction) {

            const bucket = new VehicleBucket(
                action.data, action.fromTime, action.toTime
            )
            // probably this should do something less mutable
            state.buckets.push(bucket)
            if (!state.currentBucket) {
                state.currentBucket = bucket
            }
            // fetch more data if there is anything to fetch
            if (action.toTime <= this.configStore.state.endTime) {
                this.fetchBucket(action.toTime + 1, action.toTime + this.bucketSize)
            }
            return Object.assign({}, state)
        } else if (action instanceof SetInfoReceivedAction) {

            // start fetching buckets
            this.fetchBucket(action.info.startTime, action.info.startTime + this.bucketSize)
        }
        return state
    }

    private getIndex(time: number) {
        return Math.floor((time - this.configStore.state.startTime) / this.bucketSize)
    }

    private fetchBucket(fromTime: number, toTime: number) {

        // fire and forget
        this.api.getTrajectories(fromTime, toTime)
    }
}