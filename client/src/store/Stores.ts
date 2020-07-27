import ConfigurationStore from "@/store/ConfigurationStore";
import NetworkStore from "@/store/NetworkStore";
import ClockStore from "@/store/ClockStore";

let configStore: ConfigurationStore
let networkStore: NetworkStore
let clockStore: ClockStore

interface StoresInput {
    configStore: ConfigurationStore,
    networkStore: NetworkStore,
    clockStore: ClockStore
}

export const setStores = function (stores: StoresInput) {

    configStore = stores.configStore
    networkStore = stores.networkStore
    clockStore = stores.clockStore
}

export const getConfigStore = () => configStore
export const getNetworkStore = () => networkStore
export const getClockStore = () => clockStore