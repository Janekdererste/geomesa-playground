import * as React from 'react'
import {MapComponent} from "@/MapComponent";
import {ConfigurationState} from "@/store/ConfigurationStore";
import {ClockState} from "@/store/ClockStore";
import {getClockStore, getConfigStore} from "@/store/Stores";
import {ClockComponent} from "@/Clock";

var styles = require('./App.css') as any

interface AppProps {
}

interface AppState {
    configState: ConfigurationState
    clockState: ClockState
}

export class App extends React.Component<AppProps, AppState> {

    private configStore = getConfigStore()
    private clockStore = getClockStore()

    constructor(props: AppProps) {
        super(props);

        this.configStore.register(() => this.setState({configState: this.configStore.state}))
        this.clockStore.register(() => this.setState({clockState: this.clockStore.state}))
        this.state = {
            configState: this.configStore.state,
            clockState: this.clockStore.state
        }
    }

    render() {
        return (
            <div className={styles.appWrapper}>
                <MapComponent/>
                <div className={styles.clock}>
                    <ClockComponent time={this.state.clockState.time}/>
                </div>
            </div>
        )
    }
}