export class AnimationClock {

    private animationTime: number
    private simulationClock: SimulationClock

    constructor(simulationClock: SimulationClock) {
        this.animationTime = simulationClock.Time * 1000
        this.simulationClock = simulationClock
    }

    get AnimationTime() {
        return this.animationTime / 1000
    }

    /**
     * This assumes the time is advanced 60 times per second thus advancing time by 16.7 milliseconds
     */
    public advanceTime() {

        if (this.animationTime > 24 * 3600 * 1000) this.animationTime = 5 * 3600 * 1000
        this.animationTime += 16.7 * this.simulationClock.PlaybackSpeed
        const simulationTime = this.simulationClock.Time

        if (Math.floor(this.AnimationTime) > simulationTime) {
            this.simulationClock.Time = simulationTime + 1
        }
    }
}

export class SimulationClock {

    private playbackSpeed: number = 200
    private time: number
    private startTime: number
    private endTime: number

    constructor(startTime: number, endTime: number) {

        this.startTime = startTime
        this.endTime = endTime
        this.time = startTime
    }

    get Time() {
        return this.time
    }

    set Time(value: number) {
        if (value % 100 === 0)
            console.log(value)
        this.time = value
    }

    get PlaybackSpeed() {
        return this.playbackSpeed
    }
}