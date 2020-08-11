import ConfigurationStore from "@/store/ConfigurationStore";
import NetworkStore from "@/store/NetworkStore";
import PlaybackStore from "@/store/PlaybackStore";
import LinkTripStore from "@/store/LinkTripStore";
import PlanStore from "@/store/PlanStore";

let configStore: ConfigurationStore
let networkStore: NetworkStore
let playbackStore: PlaybackStore
let linkTripStore: LinkTripStore
let planStore: PlanStore

interface StoresInput {
    configStore: ConfigurationStore,
    networkStore: NetworkStore,
    playbackStore: PlaybackStore,
    linkTripStore: LinkTripStore,
    planStore: PlanStore
}

export const setStores = function (stores: StoresInput) {

    configStore = stores.configStore
    networkStore = stores.networkStore
    playbackStore = stores.playbackStore
    linkTripStore = stores.linkTripStore
    planStore = stores.planStore
}

export const getConfigStore = () => configStore
export const getNetworkStore = () => networkStore
export const getPlaybackStore = () => playbackStore
export const getLinkTripStore = () => linkTripStore
export const getPlanStore = () => planStore