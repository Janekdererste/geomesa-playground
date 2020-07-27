import {Link} from "@/API";

export default class Network {

    private ids: String[]

    constructor(links: Link[]) {

        const valuesPerLink = 6
        this._coords = new Float32Array(links.length * valuesPerLink)
        this.ids = []

        links.forEach((value, i) => {

            // put the coords into the typed array, using three dimensions until we use custom shader
            const coordLinkIndex = i * valuesPerLink
            this._coords[coordLinkIndex] = value.from.x
            this._coords[coordLinkIndex + 1] = value.from.y
            this._coords[coordLinkIndex + 2] = 0
            this._coords[coordLinkIndex + 3] = value.to.x
            this._coords[coordLinkIndex + 4] = value.to.y
            this._coords[coordLinkIndex + 5] = 0

            // also save the id
            this.ids.push(value.linkId)
        })
    }

    private _coords: Float32Array

    get coords() {
        return this._coords
    }
}