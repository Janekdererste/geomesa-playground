import Map from 'ol/Map'
import View from 'ol/View'
import TileLayer from 'ol/layer/Tile'
import OSM from 'ol/source/OSM'

const styles = require('./OpenLayers.css') as any

export default class OpenLayers {
    private map: Map;

    constructor(container: HTMLDivElement) {

        // create openlayers map
        this.map = new Map({
            target: container,
            layers: [new TileLayer({source: new OSM()})],
            view: new View({center: [0, 0], zoom: 12}),
        })
    }

    updateSize() {
        this.map.updateSize()
    }
}