import Map from 'ol/Map'
import TileLayer from "ol/layer/Tile";
import {OSM} from "ol/source";
import {View} from "ol";
import {ObjectEvent} from "ol/Object";
import Rectangle from "@/Rectangle";

const styles = require('./MapLayer.css') as any

export interface MapLayerProps {
    container: HTMLDivElement,
    onSizeChanged: (extent: [number, number]) => void,
    onFinishRender: (extend: Rectangle) => void
}

export default class MapLayer {

    private map: Map;
    private overlay: HTMLCanvasElement

    constructor(props: MapLayerProps) {

        this.map = new Map({
            target: props.container,
            layers: [new TileLayer({source: new OSM()})],
            view: new View({center: [0, 0], zoom: 5})
        })

        // add a second canvas for the ThreeJS Overlay
        this.overlay = document.createElement('canvas')
        this.overlay.className = styles.overlay
        this.overlay.id = 'overlay-canvas '
        this.map.getViewport().appendChild(this.overlay)

        this.map.on('change:size', e => {
            const newValue = (e as ObjectEvent).target.get(e.key)
            props.onSizeChanged(newValue)
        })

        this.map.on('postrender', () => {
            const extent = this.map.getView().calculateExtent();
            props.onFinishRender(Rectangle.createFromExtend(extent))
        })
    }

    get Overlay() {
        return this.overlay
    }

    updateSize() {
        this.map.updateSize()
    }
}