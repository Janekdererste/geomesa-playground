import React from 'react'
import OpenLayers from "@/OpenLayers";
import Network from "@/Network";
import {Link} from "@/NetworkResponse";

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

        this.openlayers = new OpenLayers(this.mapContainer.current)
        this.setMapSizeAfterTimeout(500)

        const result = await fetch("http://localhost:8080/network", {
            mode: 'cors',
            method: 'GET',
            headers: {'Content-Type': 'application/json'},
        })

        if (result.ok) {
            const networkResponse = await result.json() as Link[]
            this.openlayers.addNetwork(new Network(networkResponse))
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