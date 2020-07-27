import React from "react";

export interface ClockComponentProps {
    time: number
}

export const ClockComponent = (props: ClockComponentProps) => {
    return (
        <div><span>Clock</span></div>
    )
}