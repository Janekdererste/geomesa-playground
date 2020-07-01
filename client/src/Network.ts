import {Link} from "@/API";

export default class Network {

    private coords: Float32Array
    private ids: String[]

    constructor(links: Link[]) {

        const valuesPerLink = 6
        this.coords = new Float32Array(links.length * valuesPerLink)
        this.ids = []

        links.forEach((value, i) => {

            // put the coords into the typed array, using three dimensions until we use custom shader
            const coordLinkIndex = i * valuesPerLink
            this.coords[coordLinkIndex] = value.from.x
            this.coords[coordLinkIndex + 1] = value.from.y
            this.coords[coordLinkIndex + 2] = 0
            this.coords[coordLinkIndex + 3] = value.to.x
            this.coords[coordLinkIndex + 4] = value.to.y
            this.coords[coordLinkIndex + 5] = 0

            // also save the id
            this.ids.push(value.linkId)
        })
    }

    get Coords() {
        return this.coords
    }
}