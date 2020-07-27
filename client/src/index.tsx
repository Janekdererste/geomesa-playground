import {App} from '@/App'
import ReactDOM from 'react-dom'
import * as React from 'react'
import {getClockStore, getConfigStore, getNetworkStore, setStores} from "@/store/Stores";
import ConfigurationStore from "@/store/ConfigurationStore";
import NetworkStore from "@/store/NetworkStore";
import Dispatcher from "@/store/Dispatcher";
import Api from "@/API";
import ClockStore from "@/store/ClockStore";

const api = new Api('http://localhost:8080')

// set up state management
setStores({
    configStore: new ConfigurationStore(),
    networkStore: new NetworkStore(api),
    clockStore: new ClockStore(),
})

// register stores at dispatcher
Dispatcher.register(getConfigStore())
Dispatcher.register(getNetworkStore())
Dispatcher.register(getClockStore())

api.getInfo()

const root = document.createElement('div') as HTMLDivElement
root.id = 'root'
root.style.height = '100%'
document.body.appendChild(root)

ReactDOM.render(<App/>, root)