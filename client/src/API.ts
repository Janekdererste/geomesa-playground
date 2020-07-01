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

