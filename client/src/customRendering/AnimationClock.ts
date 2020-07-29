import {getPlaybackStore} from "@/store/Stores";
import Dispatcher from "@/store/Dispatcher";
import {AdvanceTimeAction} from "@/store/PlaybackStore";

export class AnimationClock {

    constructor(startTime: number, endTime: number) {

        // animation time is in ms
        this._animationTime = startTime * 1000
        this.startTime = startTime * 1000
        this.endTime = endTime * 1000
    }

    private startTime: number
    private endTime: number;
    private clockStore = getPlaybackStore()

    private _animationTime: number

    get animationTime() {
        return this._animationTime / 1000
    }

    /**
     * This assumes the time is advanced 60 times per second thus advancing time by 16.7 milliseconds
     */
    public advanceTime() {

        // increment time by 16.7ms * playbackSpeed factor. This assumes 60fps
        this._animationTime = this._animationTime + 16.7 * this.clockStore.state.playbackSpeed

        if (this._animationTime > this.endTime) this._animationTime = this.startTime

        const simulationTime = this.clockStore.state.time
        const roundedAnimationTime = Math.floor(this.animationTime)
        if (roundedAnimationTime > simulationTime) {
            Dispatcher.dispatch(new AdvanceTimeAction(roundedAnimationTime))
        }
    }
}