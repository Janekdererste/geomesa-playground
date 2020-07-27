import * as React from 'react'
import {MapComponent} from "@/MapComponent";
import {ConfigurationState} from "@/store/ConfigurationStore";

var styles = require('./App.css') as any

interface AppProps {
}

interface AppState {
    configState: ConfigurationState
}

export class App extends React.Component<AppProps, AppState> {

    render() {
        return (
            <div className={styles.appWrapper}>
                <MapComponent/>
            </div>
        )
    }
}