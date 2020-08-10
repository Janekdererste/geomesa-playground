import React from 'react'
import {AdvanceTime, ChangePlaybackSpeed, PlaybackState, TogglePlayback} from "@/store/PlaybackStore";
import Dispatcher from "@/store/Dispatcher"

const styles = require('./PlaybackControls.css')

export interface PlaybackControlsProps {
    playbackState: PlaybackState
}

export const PlaybackControls = (props: PlaybackControlsProps) => {

    const dispatchTime = (value: string) => {
        const newValue = Number.parseInt(value)
        Dispatcher.dispatch(new AdvanceTime(newValue))

    }

    const dispatchSpeed = (value: string) => {
        const newValue = Number.parseInt(value)
        const multiplyWith = newValue >= 0 ? newValue : newValue * -1
        Dispatcher.dispatch(new ChangePlaybackSpeed(newValue * multiplyWith))
    }

    return (
        <div className={styles.wrapper}>
            <button className={styles.mainButton}
                    onClick={() => Dispatcher.dispatch(new TogglePlayback())}>{props.playbackState.isPlaying ? 'Stop' : 'Start'}
            </button>
            <input className={styles.speedControl} type="range"
                   min="-10" max="10" step="1"
                   onInput={e => dispatchSpeed(e.currentTarget.value)}
                   onChange={e => dispatchSpeed(e.currentTarget.value)}
            />

            <span className={styles.playbackSpeed}>
                {props.playbackState.playbackSpeed}x
            </span>

            <input type="range" min={props.playbackState.startTime} max={props.playbackState.endTime} step="1"
                   onInput={e => dispatchTime(e.currentTarget.value)}
                   onChange={e => dispatchTime(e.currentTarget.value)}
                   value={props.playbackState.time}
            />
        </div>
    )
}