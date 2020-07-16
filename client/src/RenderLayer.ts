import {OrthographicCamera, Scene, WebGLRenderer} from "three";
import Rectangle from "@/Rectangle";
import Network from "@/Network";
import NetworkLayer from "@/NetworkLayer";
import TrafficAnimationLayer from "@/TrafficAnimationLayer";
import {AnimationClock, SimulationClock} from "@/Clock";
import Api from "@/API";

export interface RenderLayerProps {

    canvas: HTMLCanvasElement,
    clock: SimulationClock,
    api: Api,
    startTime: number,
    endTime: number
}

// maybe come up with a better name
export default class RenderLayer {
    private camera: OrthographicCamera;
    private renderer: WebGLRenderer;
    private canvas: HTMLCanvasElement;
    private scene = new Scene()
    private runAnimation: boolean = false;
    private animationLayer: TrafficAnimationLayer;
    private clock: AnimationClock


    constructor(props: RenderLayerProps) {

        this.canvas = props.canvas
        this.camera = new OrthographicCamera(0, 0, 0, 0, 1, 100)
        this.renderer = new WebGLRenderer({
            canvas: props.canvas,
            antialias: true,
            alpha: true
        })
        this.renderer.setSize(props.canvas.clientWidth, props.canvas.clientHeight)
        this.renderer.setPixelRatio(window.devicePixelRatio)

        // slightly inconsistent with how the network layer is added, maybe change at some point
        this.animationLayer = new TrafficAnimationLayer(props.api, props.startTime, props.endTime)
        this.scene.add(this.animationLayer.sceneObject)

        this.clock = new AnimationClock(props.clock)
    }

    addNetwork(network: Network) {
        const networkLayer = new NetworkLayer(network)
        // probably we need to keep a reference to the network layer
        this.scene.add(networkLayer.sceneObject)
        if (!this.runAnimation) this.renderSingleFrame()
    }

    adjustExtent(extent: Rectangle) {
        this.adjustCamera(extent)
        if (!this.runAnimation)
            this.renderSingleFrame()
    }

    adjustSize(extent: [number, number]) {
        this.canvas.width = extent[0]
        this.canvas.height = extent[1]
        this.renderer.setSize(extent[0], extent[1])

        // all the other stuff should be handled by adjust camera
    }

    startAnimation() {
        if (!this.runAnimation) {
            this.runAnimation = true
            this.renderAnimation()
        }
    }

    stopAnimation() {
        if (this.runAnimation) {
            this.runAnimation = false
        }
    }

    private renderAnimation() {
        if (this.runAnimation) {
            requestAnimationFrame(() => this.renderAnimation())
            this.clock.advanceTime()
            this.animationLayer.updateTime(this.clock.AnimationTime)
            this.renderSingleFrame()
        }
    }

    private renderSingleFrame() {
        this.renderer.render(this.scene, this.camera)
    }

    private adjustCamera(extent: Rectangle) {

        this.camera.left = extent.Left
        this.camera.right = extent.Right
        this.camera.top = extent.Top
        this.camera.bottom = extent.Bottom
        this.camera.updateProjectionMatrix()
    }
}