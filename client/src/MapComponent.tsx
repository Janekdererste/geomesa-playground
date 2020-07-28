import React from 'react'
import MapLayer from "@/customRendering/MapLayer";
import Rectangle from "@/Rectangle";
import RenderLayer from "@/customRendering/RenderLayer";
import {PlaybackState} from "@/store/PlaybackStore";

const styles = require('./MapComponent.css') as any

export interface MapProps {
    playbackState: PlaybackState
}

export class MapComponent extends React.Component<MapProps> {

    private mapContainer: React.RefObject<HTMLDivElement>
    private renderLayer!: RenderLayer

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

        this.renderLayer = new RenderLayer({
            canvas: this.mapLayer.Overlay,
        })
    }

    public componentDidUpdate(prevProps: Readonly<MapProps>, prevState: Readonly<{}>, snapshot?: any) {

        if (this.props.playbackState.isPlaying) this.renderLayer.startAnimation()
        else this.renderLayer.stopAnimation()
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