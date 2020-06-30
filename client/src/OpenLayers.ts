import Map from 'ol/Map'
import View from 'ol/View'
import TileLayer from 'ol/layer/Tile'
import OSM from 'ol/source/OSM'
import {OrthographicCamera, WebGLRenderer} from "three";
import MapBrowserEvent from "ol/MapBrowserEvent";
import {ObjectEvent} from "ol/Object";
import Rectangle from "@/Rectangle";

const styles = require('./OpenLayers.css') as any

export default class OpenLayers {
    private map: Map;
    private canvas: HTMLCanvasElement;
    private camera: OrthographicCamera;
    private renderer: WebGLRenderer;

    constructor(container: HTMLDivElement) {

        // create openlayers map
        this.map = new Map({
            target: container,
            layers: [new TileLayer({source: new OSM()})],
            view: new View({center: [0, 0], zoom: 12}),
        })

        // add a second canvas for the ThreeJS Overlay
        this.canvas = document.createElement('canvas')
        this.canvas.className = styles.overlay
        this.canvas.id = 'overlay-canvas '
        this.map.getViewport().appendChild(this.canvas)

        this.camera = new OrthographicCamera(0, 0, 0, 0, 1, 100)
        this.renderer = new WebGLRenderer({
            canvas: this.canvas,
            antialias: true,
            alpha: true,
        })
        this.renderer.setSize(this.canvas.clientWidth, this.canvas.clientHeight)
        this.renderer.setPixelRatio(window.devicePixelRatio)

        // bind to some openlayers events to move the threejs scene/camera accordingly
        this.map.on('change:size', e => this.onSizeChanged(e as ObjectEvent))
        this.map.on('postrender', () => this.onOpenlayersFinishedRender())
        this.map.on('click', e => OpenLayers.onMapClicked(e as MapBrowserEvent))
    }

    updateSize() {
        this.map.updateSize()
    }

    private static renderOneFrame() {
        console.log("Here should be some render action")
        // this.renderer.render(this.layers.Scene, this.camera)
        // this.labelRenderer.render(this.layers.Scene, this.camera)
    }

    private static onMapClicked(event: MapBrowserEvent) {
        console.log("here should be some click action")
        //this.layers.intersectCoordinate(event.coordinate, this.clock.AnimationTime)
    }

    private onSizeChanged(event: ObjectEvent) {
        const newValue = event.target.get(event.key)
        this.canvas.width = newValue[0]
        this.canvas.height = newValue[1]
        this.renderer.setSize(this.canvas.width, this.canvas.height)
        // this.labelRenderer.setSize(this.canvas.width, this.canvas.height)
        const extend = this.map.getView().calculateExtent()
        this.adjustCamera(Rectangle.createFromExtend(extend))
        OpenLayers.renderOneFrame()
    }

    private onOpenlayersFinishedRender() {
        const extend = this.map.getView().calculateExtent()
        this.adjustCamera(Rectangle.createFromExtend(extend))
        OpenLayers.renderOneFrame()
    }

    private adjustCamera(bounds: Rectangle) {
        this.camera.left = bounds.Left
        this.camera.right = bounds.Right
        this.camera.top = bounds.Top
        this.camera.bottom = bounds.Bottom
        this.camera.updateProjectionMatrix()
    }
}