import Store from "@/store/Store";
import {Action} from "@/store/Dispatcher";
import {SetInfoReceivedAction} from "@/API";

export interface PlaybackState {
    time: number
    playbackSpeed: number
    isPlaying: boolean
    startTime: number
    endTime: number
}

export class AdvanceTime implements Action {

    constructor(time: number) {
        this._time = time
    }

    private _time: number

    public get time() {
        return this._time
    }
}

export class ChangePlaybackSpeed implements Action {

    constructor(speed: number) {
        this._speed = speed
    }

    private _speed: number

    public get speed() {
        return this._speed
    }
}

export class TogglePlayback implements Action {
}

export default class PlaybackStore extends Store<PlaybackState> {
    getInitialState(): PlaybackState {
        return {time: 0, playbackSpeed: 100, isPlaying: false, startTime: 0, endTime: 0}
    }

    reduce(state: PlaybackState, action: Action): PlaybackState {

        if (action instanceof AdvanceTime) {
            return Object.assign({}, state, {time: action.time})
        } else if (action instanceof TogglePlayback) {
            return Object.assign({}, state, {isPlaying: !state.isPlaying})
        } else if (action instanceof ChangePlaybackSpeed) {
            return Object.assign({}, state, {playbackSpeed: action.speed})
        } else if (action instanceof SetInfoReceivedAction) {
            return Object.assign({}, state, {
                startTime: action.info.startTime, endTime: action.info.endTime, time: action.info.startTime
            })
        }
        return state;
    }

}