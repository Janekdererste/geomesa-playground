import {getPlaybackStore} from "@/store/Stores";
import Dispatcher from "@/store/Dispatcher";
import {AdvanceTimeAction} from "@/store/PlaybackStore";

export class AnimationClock {

    private animationTime: number
    private startTime: number
    private endTime: number;
    private clockStore = getPlaybackStore()

    constructor(startTime: number, endTime: number) {
        this.animationTime = startTime
        this.startTime = startTime
        this.endTime = endTime
    }

    get AnimationTime() {
        return this.animationTime / 1000
    }

    /**
     * This assumes the time is advanced 60 times per second thus advancing time by 16.7 milliseconds
     */
    public advanceTime() {

        this.animationTime = 16.7 * this.clockStore.state.playbackSpeed

        if (this.animationTime > this.endTime) this.animationTime = this.startTime

        const simulationTime = this.clockStore.state.time
        if (Math.floor(this.AnimationTime) > simulationTime) {
            Dispatcher.dispatch(new AdvanceTimeAction(simulationTime + 1))
        }
    }
}