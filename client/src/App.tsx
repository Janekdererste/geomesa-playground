import * as React from 'react'
import {MapComponent} from "@/MapComponent";
import {ConfigurationState} from "@/store/ConfigurationStore";
import {PlaybackState} from "@/store/PlaybackStore";
import {getConfigStore, getPlanStore, getPlaybackStore} from "@/store/Stores";
import {ClockComponent} from "@/Clock";
import {PlaybackControls} from "@/PlaybackControls";
import {Sidebar} from "@/Sidebar";
import {PlanState} from "@/store/PlanStore";

var styles = require('./App.css') as any

interface AppProps {
}

interface AppState {
    configState: ConfigurationState
    playbackState: PlaybackState
    planState: PlanState
}

export class App extends React.Component<AppProps, AppState> {

    private configStore = getConfigStore()
    private playbackStore = getPlaybackStore()
    private planStore = getPlanStore()

    constructor(props: AppProps) {
        super(props);

        this.configStore.register(() => this.setState({configState: this.configStore.state}))
        this.playbackStore.register(() => this.setState({playbackState: this.playbackStore.state}))
        this.planStore.register(() => this.setState({planState: this.planStore.state}))

        // set initial state
        this.state = {
            configState: this.configStore.state,
            playbackState: this.playbackStore.state,
            planState: this.planStore.state
        }
    }

    render() {
        return (
            <div className={styles.appWrapper}>
                <MapComponent playbackState={this.state.playbackState}/>
                <div className={styles.clock}>
                    <ClockComponent time={this.state.playbackState.time}/>
                </div>
                <div className={styles.sidebar}>
                    <Sidebar planState={this.state.planState}/>
                </div>
                <div className={styles.controls}>
                    <PlaybackControls playbackState={this.state.playbackState}/>
                </div>
            </div>
        )
    }
}