import React from 'react'
import OpenLayers from "@/OpenLayers";
import Network from "@/Network";
import {Link, SetInfo, Trajectory} from "@/API";
import {SimulationClock} from "@/Clock";

const styles = require('./MapComponent.css') as any

export interface MapProps {
}

export class MapComponent extends React.Component<MapProps> {

    private mapContainer: React.RefObject<HTMLDivElement>
    private openlayers!: OpenLayers

    constructor(props: MapProps) {
        super(props)
        this.mapContainer = React.createRef<HTMLDivElement>()
    }

    public async componentDidMount() {

        if (!this.mapContainer.current) throw new Error('map div was not set!')


        const infoResult = await fetch("http://localhost:8080/info", {
            mode: 'cors',
            method: 'GET',
            headers: {'Content-Type': 'application/json'},
        })

        if (infoResult.ok) {
            const infoResponse = await infoResult.json() as SetInfo
            const clock = new SimulationClock(infoResponse.startTime, infoResponse.endTime)
            const center: [number, number] = [infoResponse.bbox.maxX - infoResponse.bbox.minX, infoResponse.bbox.maxY - infoResponse.bbox.minY]
            this.openlayers = new OpenLayers(this.mapContainer.current, clock, center)
            this.setMapSizeAfterTimeout(500)
        }

        const result = await fetch("http://localhost:8080/network", {
            mode: 'cors',
            method: 'GET',
            headers: {'Content-Type': 'application/json'},
        })

        if (result.ok) {
            const networkResponse = await result.json() as Link[]
            this.openlayers.addNetwork(new Network(networkResponse))
        }

        const trajectoryResult = await fetch("http://localhost:8080/trajectory", {
            mode: 'cors',
            method: 'GET',
            headers: {'Content-Type': 'application/json'},
        })

        if (trajectoryResult.ok) {
            const trajectoryResponse = await trajectoryResult.json() as Trajectory[]
            this.openlayers.addTrajectories(trajectoryResponse);
            this.openlayers.startAnimation()
        }
    }

    public render() {
        return (
            <div className={styles.map} ref={this.mapContainer}/>
        )
    }

    private setMapSizeAfterTimeout(timeout: number) {
        setTimeout(() => {
            if (this.mapContainer.current && this.mapContainer.current.clientHeight > 0) {
                this.openlayers.updateSize()
            } else {
                this.setMapSizeAfterTimeout(timeout * 2)
            }
        }, timeout)
    }
}