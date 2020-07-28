import React from 'react'
import {PlaybackState, TogglePlaybackAction} from "@/store/PlaybackStore";
import Dispatcher from "@/store/Dispatcher";

export interface PlaybackControlsProps {
    playbackState: PlaybackState
}

export const PlaybackControls = (props: PlaybackControlsProps) => {

    return (
        <div>
            <button
                onClick={() => Dispatcher.dispatch(new TogglePlaybackAction())}>{props.playbackState.isPlaying ? 'Stop' : 'Start'}</button>
        </div>
    )
}