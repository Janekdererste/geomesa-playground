import {Coord, LinkTrip} from "@/API";

export interface MovingPoints {
    positions: Float32Array
    toPositions: Float32Array
    fromTimes: Float32Array
    toTimes: Float32Array
    // colors: Float32Array do this later
}

export default class TrajectoryMapper {

    private vehicles: MovingPoints

    constructor(trajectories: LinkTrip[]) {

        this.vehicles = this.createVehicles(trajectories)
    }

    public get Vehicles() {
        return this.vehicles
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

            TrajectoryMapper.pushCoord(fromCoords, linkTrip.from)
            TrajectoryMapper.pushCoord(toCoords, linkTrip.to)
            fromTimes.push(linkTrip.fromTime)
            toTimes.push(linkTrip.toTime)
        })

        console.log(fromCoords)
        console.log(fromTimes)
        console.log(toCoords)
        console.log(toTimes)

        return {
            fromTimes: new Float32Array(fromTimes),
            toTimes: new Float32Array(toTimes),
            positions: new Float32Array(fromCoords),
            toPositions: new Float32Array(toCoords),
        }
    }
}