import {Coord, Trajectory} from "@/API";

export interface MovingPoints {
    positions: Float32Array
    toPosition: Float32Array
    fromTimes: Float32Array
    toTimes: Float32Array
    // colors: Float32Array do this later
}

export default class TrajectoryMapper {

    private vehicles: MovingPoints

    constructor(trajectories: Trajectory[]) {

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

    private createVehicles(trajectories: Trajectory[]) {

        const fromCoords: number[] = []
        const toCoords: number[] = []
        const times: number[] = []
        const toTimes: number[] = []

        trajectories.forEach(trajectory => {

            for (let i = 0; i < trajectory.coords.length - 1; i++) {

                const coord = trajectory.coords[i]
                TrajectoryMapper.pushCoord(fromCoords, coord)
                times.push(trajectory.times[i])

                const toCoord = trajectory.coords[i + 1]
                TrajectoryMapper.pushCoord(toCoords, toCoord)
                toTimes.push(trajectory.times[i + 1])
            }
        })

        return {
            fromTimes: new Float32Array(times),
            toTimes: new Float32Array(toTimes),
            positions: new Float32Array(fromCoords),
            toPosition: new Float32Array(toCoords),
        }
    }
}