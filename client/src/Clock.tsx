import React from "react";

const styles = require('./Clock.css')

export interface ClockComponentProps {
    time: number
}

export const ClockComponent = (props: ClockComponentProps) => {

    const formatter = new Intl.NumberFormat('en-US', {style: 'decimal', minimumIntegerDigits: 2})

    const convertHours = (time: number) => {
        return formatter.format(Math.floor(time / 3600))
    }

    const convertMinutes = (time: number) => {
        const minutes = time % 3600 / 60
        return formatter.format(Math.floor(minutes))
    }

    const convertSeconds = (time: number) => {
        return formatter.format(time % 60)
    }

    return (
        <div className={styles.clock}>
            <span>{`${convertHours(props.time)}:${convertMinutes(props.time)}:${convertSeconds(props.time)}`}</span>
        </div>
    )
}