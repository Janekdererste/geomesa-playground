import ConfigurationStore from "@/store/ConfigurationStore";
import NetworkStore from "@/store/NetworkStore";
import PlaybackStore from "@/store/PlaybackStore";
import LinkTripStore from "@/store/LinkTripStore";

let configStore: ConfigurationStore
let networkStore: NetworkStore
let playbackStore: PlaybackStore
let linkTripStore: LinkTripStore

interface StoresInput {
    configStore: ConfigurationStore,
    networkStore: NetworkStore,
    playbackStore: PlaybackStore,
    linkTripStore: LinkTripStore
}

export const setStores = function (stores: StoresInput) {

    configStore = stores.configStore
    networkStore = stores.networkStore
    playbackStore = stores.playbackStore
    linkTripStore = stores.linkTripStore
}

export const getConfigStore = () => configStore
export const getNetworkStore = () => networkStore
export const getPlaybackStore = () => playbackStore
export const getLinkTripStore = () => linkTripStore