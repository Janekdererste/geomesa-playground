import React from 'react'
import Network from "@/Network";
import Api from "@/API";
import {SimulationClock} from "@/Clock";
import MapLayer from "@/MapLayer";
import Rectangle from "@/Rectangle";
import RenderLayer from "@/RenderLayer";

const styles = require('./MapComponent.css') as any

export interface MapProps {
}

export class MapComponent extends React.Component<MapProps> {

    private mapContainer: React.RefObject<HTMLDivElement>
    private renderLayer!: RenderLayer
    private api = new Api("http://localhost:8080")

    private mapLayer!: MapLayer

    constructor(props: MapProps) {
        super(props)
        this.mapContainer = React.createRef<HTMLDivElement>()
    }

    public async componentDidMount() {

        if (!this.mapContainer.current) throw new Error('map div was not set!')

        this.mapLayer = new MapLayer({
            container: this.mapContainer.current,
            onSizeChanged: extent => this.onSizeChanged(extent),
            onFinishRender: extent => this.onFinishRender(extent)
        })

        this.setMapSizeAfterTimeout(500)

        const setInfo = await this.api.getInfo()

        this.renderLayer = new RenderLayer({
            canvas: this.mapLayer.Overlay,
            clock: new SimulationClock(setInfo.startTime, setInfo.endTime),
            api: this.api,
            startTime: setInfo.startTime,
            endTime: setInfo.endTime
        })

        const networkResponse = await this.api.getNetwork("car")
        this.renderLayer.addNetwork(new Network(networkResponse))

        // fetch only the first bucket of agents
        //   const trajectories = await this.api.getTrajectories(setInfo.startTime, setInfo.startTime + 3599)
        //   this.renderLayer.updateTrajectories(trajectories)
        this.renderLayer.startAnimation()
    }

    public render() {
        return (
            <div className={styles.map} ref={this.mapContainer}/>
        )
    }

    private setMapSizeAfterTimeout(timeout: number) {
        setTimeout(() => {
            if (this.mapContainer.current && this.mapContainer.current.clientHeight > 0) {
                this.mapLayer.updateSize()
            } else {
                this.setMapSizeAfterTimeout(timeout * 2)
            }
        }, timeout)
    }

    private onSizeChanged(extent: [number, number]) {
        // check for presence since render layer may not be ready yet
        if (this.renderLayer)
            this.renderLayer.adjustSize(extent)
    }

    private onFinishRender(extent: Rectangle) {
        if (this.renderLayer)
            this.renderLayer.adjustExtent(extent)
    }
}