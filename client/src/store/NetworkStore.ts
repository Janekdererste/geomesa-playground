import {Action} from "@/store/Dispatcher";
import Store from "@/store/Store";
import Api, {LinksReceivedAction, SetInfoReceivedAction} from "@/API";
import Network from "@/Network";

export interface NetworkState {
    network: Network
}

export default class NetworkStore extends Store<NetworkState> {

    private api: Api

    constructor(api: Api) {
        super();
        this.api = api
    }

    getInitialState(): NetworkState {
        return {
            network: new Network([])
        }
    }

    reduce(state: NetworkState, action: Action): NetworkState {

        if (action instanceof LinksReceivedAction) {
            return Object.assign({network: new Network(action.data)})
        } else if (action instanceof SetInfoReceivedAction) {
            // hard code car network for now and do something more elaborated based on the set info later
            // also consider loading something other than the whole network
            this.api.getNetwork('car')
        }
        return state;
    }
}