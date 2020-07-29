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

export class AdvanceTimeAction {

    constructor(time: number) {
        this._time = time
    }

    private _time: number

    public get time() {
        return this._time
    }
}

export class TogglePlaybackAction implements Action {
}

export default class PlaybackStore extends Store<PlaybackState> {
    getInitialState(): PlaybackState {
        return {time: 0, playbackSpeed: 100, isPlaying: false, startTime: 0, endTime: 0}
    }

    reduce(state: PlaybackState, action: Action): PlaybackState {

        if (action instanceof AdvanceTimeAction) {
            return Object.assign({}, state, {time: action.time})
        } else if (action instanceof TogglePlaybackAction) {
            return Object.assign({}, state, {isPlaying: !state.isPlaying})
        } else if (action instanceof SetInfoReceivedAction) {
            return Object.assign({}, state, {
                startTime: action.info.startTime, endTime: action.info.endTime
            })
        }
        return state;
    }

}