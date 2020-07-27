import {Action} from "@/store/Dispatcher";
import Rectangle from "@/Rectangle";
import {SetInfoReceivedAction} from "@/API";
import Store from "@/store/Store";

export interface ConfigurationState {
    startTime: number,
    endTime: number,
    bbox: Rectangle
    modesInNetwork: String[]
    serverConfigWasPulled: boolean
}

export default class ConfigurationStore extends Store<ConfigurationState> {

    getInitialState(): ConfigurationState {
        return {
            bbox: new Rectangle(0, 0, 0, 0),
            endTime: 0,
            startTime: 0,
            modesInNetwork: [],
            serverConfigWasPulled: false
        }
    }

    reduce(state: ConfigurationState, action: Action): ConfigurationState {

        if (action instanceof SetInfoReceivedAction) {
            return {
                bbox: Rectangle.createFromExtend([action.info.bbox.minX, action.info.bbox.minY, action.info.bbox.maxX, action.info.bbox.maxY]),
                modesInNetwork: action.info.modesInNetwork,
                startTime: action.info.startTime,
                endTime: action.info.endTime,
                serverConfigWasPulled: true
            }
        }
        return state
    }
}