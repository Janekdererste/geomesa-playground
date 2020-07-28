import {App} from '@/App'
import ReactDOM from 'react-dom'
import * as React from 'react'
import {getConfigStore, getLinkTripStore, getNetworkStore, getPlaybackStore, setStores} from "@/store/Stores";
import ConfigurationStore from "@/store/ConfigurationStore";
import NetworkStore from "@/store/NetworkStore";
import Dispatcher from "@/store/Dispatcher";
import Api from "@/API";
import PlaybackStore from "@/store/PlaybackStore";
import LinkTripStore from "@/store/LinkTripStore";

const api = new Api('http://localhost:8080')

// set up state management
const configStore = new ConfigurationStore()
setStores({
    configStore: configStore,
    networkStore: new NetworkStore(api),
    playbackStore: new PlaybackStore(),
    linkTripStore: new LinkTripStore(api, configStore)
})

// register stores at dispatcher
Dispatcher.register(getConfigStore())
Dispatcher.register(getNetworkStore())
Dispatcher.register(getPlaybackStore())
Dispatcher.register(getLinkTripStore())

api.getInfo()

const root = document.createElement('div') as HTMLDivElement
root.id = 'root'
root.style.height = '100%'
document.body.appendChild(root)

ReactDOM.render(<App/>, root)