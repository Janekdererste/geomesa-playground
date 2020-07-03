export interface Link {

    from: Coord
    to: Coord
    linkId: string
}

export interface Coord {

    x: number
    y: number
}

export interface Trajectory {

    coords: Coord[]
    times: number[]
}

export interface SetInfo {

    bbox: Rect
    startTime: number
    endTime: number

}

export interface Rect {

    minX: number
    minY: number
    maxX: number
    maxY: number
}

