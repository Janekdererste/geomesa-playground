import {getPlaybackStore} from "@/store/Stores";
import Dispatcher from "@/store/Dispatcher";
import {AdvanceTime} from "@/store/PlaybackStore";

export class AnimationClock {

    private playbackStore = getPlaybackStore()

    private startTime: number
    private endTime: number;

    constructor(startTime: number, endTime: number, currentTime: number) {

        // animation time is in ms
        this._animationTime = currentTime * 1000
        this.startTime = startTime * 1000
        this.endTime = endTime * 1000
    }

    private _animationTime: number

    get animationTime() {
        return this._animationTime / 1000
    }

    set animationTime(value: number) {
        this._animationTime = value * 1000
    }

    /**
     * This assumes the time is advanced 60 times per second thus advancing time by 16.7 milliseconds
     */
    public advanceTime() {

        // increment time by 16.7ms * playbackSpeed factor. This assumes 60fps
        this._animationTime = this._animationTime + 16.7 * this.playbackStore.state.playbackSpeed

        if (this._animationTime > this.endTime) this._animationTime = this.startTime

        const simulationTime = this.playbackStore.state.time
        const roundedAnimationTime = Math.floor(this.animationTime)
        if (roundedAnimationTime !== simulationTime) {
            Dispatcher.dispatch(new AdvanceTime(roundedAnimationTime))
        }
    }
}