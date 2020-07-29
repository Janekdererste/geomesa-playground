import React from 'react'
import {AdvanceTimeAction, PlaybackState, TogglePlaybackAction} from "@/store/PlaybackStore";
import Dispatcher from "@/store/Dispatcher"

const styles = require('./PlaybackControls.css')

export interface PlaybackControlsProps {
    playbackState: PlaybackState
}

export const PlaybackControls = (props: PlaybackControlsProps) => {

    const dispatchValue = (value: string) => {
        const newValue = Number.parseInt(value)
        Dispatcher.dispatch(new AdvanceTimeAction(newValue))

    }
    return (
        <div className={styles.wrapper}>
            <button className={styles.mainButton}
                    onClick={() => Dispatcher.dispatch(new TogglePlaybackAction())}>{props.playbackState.isPlaying ? 'Stop' : 'Start'}
            </button>
            <input min={props.playbackState.startTime} max={props.playbackState.endTime} step="1"
                   onInput={e => dispatchValue(e.currentTarget.value)}
                   onChange={e => dispatchValue(e.currentTarget.value)}
                   value={props.playbackState.time}
                   className={styles.slider} type="range"/>
        </div>
    )
}