import Dispatcher, {Action} from "@/store/Dispatcher";

export interface Duration {

    startTime: number
    endTime: number
}

export interface Line {
    from: Coord
    to: Coord
}

export interface Coord {

    x: number
    y: number
}

export interface Rect {

    minX: number
    minY: number
    maxX: number
    maxY: number
}

export interface Leg extends Duration, Line {
    type: string
    mode: string
}

export interface Activity extends Duration {

    coordinate: Coord
    type: string
    linkId: string
    facilityId?: string
}

export interface LinkTrip {

    from: Coord
    to: Coord
    fromTime: number
    toTime: number
    mode: String
}

export interface Plan {

    elements: (Activity | Leg) []
}

export interface Link extends Line {

    linkId: string
}

export interface SetInfo {

    bbox: Rect
    startTime: number
    endTime: number
    modesInNetwork: String[]
}

export class SetInfoReceivedAction implements Action {
    constructor(info: SetInfo) {
        this._info = info
    }

    private _info: SetInfo

    public get info() {
        return this._info
    }
}

export class LinksReceivedAction implements Action {
    constructor(data: Link[]) {
        this._data = data
    }

    private _data: Link[]

    public get data() {
        return this._data
    }
}

export class BucketReceivedAction implements Action {

    constructor(data: LinkTrip[], fromTime: number, toTime: number) {
        this._data = data
        this._fromTime = fromTime
        this._toTime = toTime
    }

    private _data: LinkTrip[]

    public get data() {
        return this._data
    }

    private _fromTime: number

    public get fromTime() {
        return this._fromTime
    }

    private _toTime: number

    public get toTime() {
        return this._toTime
    }
}

export class PlanReceived implements Action {

    constructor(plan: Plan) {
        this._plan = plan
    }

    private _plan: Plan

    public get plan() {
        return this._plan
    }
}

export default class Api {

    private endpoint: string

    constructor(endpoint: string) {
        this.endpoint = endpoint
    }

    public async getInfo() {

        const result = await fetch(this.endpoint + "/info", {
            mode: 'cors',
            method: 'GET',
            headers: {'Content-Type': 'application/json'},
        })

        if (result.ok) {
            const info = await result.json() as SetInfo
            Dispatcher.dispatch(new SetInfoReceivedAction(info))
        } else throw new Error("Could not fetch Set information.!!!!1!")
    }

    public async getNetwork(modes: String[]) {
        const modesparam = modes.join('&modes=')
        console.info(modesparam)
        const result = await fetch(this.endpoint + "/network?modes=" + modesparam, {
            mode: 'cors',
            method: 'GET',
            headers: {'Content-Type': 'application/json'},
        })

        if (result.ok) {
            const links = await result.json() as Link[]
            Dispatcher.dispatch(new LinksReceivedAction(links))
        } else {
            throw new Error("error while fetching network!")
        }
    }

    public async getTrajectories(fromTime: number, toTime: number) {

        const result = await fetch(this.endpoint + "/trajectory?fromTime=" + fromTime + "&toTime=" + toTime, {
            mode: 'cors',
            method: 'GET',
            headers: {'Content-Type': 'application/json'},
        })

        if (result.ok) {
            const trips = await result.json() as LinkTrip[]
            Dispatcher.dispatch(new BucketReceivedAction(trips, fromTime, toTime))
        } else throw new Error("Error while fetching trajectories")
    }

    public async getPlan(personId: string) {

        const result = await fetch(this.endpoint + "/plan/" + personId, {
            mode: 'cors',
            method: 'GET',
            headers: {'Content-Type': 'application/json'},
        })

        if (result.ok) {
            const plan = await result.json() as Plan
            Dispatcher.dispatch(new PlanReceived(plan))
        } else throw new Error("Error while fetching plan")
    }
}

