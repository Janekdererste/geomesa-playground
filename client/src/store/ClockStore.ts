import Store from "@/store/Store";
import {Action} from "@/store/Dispatcher";

export interface ClockState {
    time: number
    playbackSpeed: number
}

export class AdvancedTimeAction {

    constructor(time: number) {
        this._time = time
    }

    private _time: number

    public get time() {
        return this._time
    }
}

export default class ClockStore extends Store<ClockState> {
    getInitialState(): ClockState {
        return {time: 0, playbackSpeed: 100}
    }

    reduce(state: ClockState, action: Action): ClockState {

        if (action instanceof AdvancedTimeAction) {
            return Object.assign({time: action.time})
        }
        return state;
    }

}