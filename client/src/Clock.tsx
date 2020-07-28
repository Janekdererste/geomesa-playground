import React from "react";

export interface ClockComponentProps {
    time: number
}

export const ClockComponent = (props: ClockComponentProps) => {

    const formatTime = (time: number) => {

        const hours = Math.floor(time / 3600)
        const minutes = Math.floor((time - hours * 3600) / 60)
        const seconds = time - hours * 3600 - minutes * 60
        return `${hours}:${minutes}:${seconds}`
    }
    return (
        <div><span>{formatTime(props.time)}</span></div>
    )
}