import {OrthographicCamera, Scene, WebGLRenderer} from "three";
import Rectangle from "@/Rectangle";
import NetworkLayer from "@/customRendering/NetworkLayer";
import TrafficAnimationLayer from "@/customRendering/TrafficAnimationLayer";
import {AnimationClock} from "@/customRendering/AnimationClock";
import {getConfigStore, getNetworkStore} from "@/store/Stores";

export interface RenderLayerProps {

    canvas: HTMLCanvasElement,
}

// maybe come up with a better name
export default class RenderLayer {
    private camera: OrthographicCamera;
    private renderer: WebGLRenderer;
    private canvas: HTMLCanvasElement;
    private scene = new Scene()
    private runAnimation: boolean = false;
    private animationLayer: TrafficAnimationLayer;
    private networkLayer: NetworkLayer
    private clock?: AnimationClock

    private configStore = getConfigStore()


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

        // we assume that the config only changes once for now. If there are more config params which can be
        // changed by the user this needs to become more elaborate
        this.configStore.register(() => {
            this.clock = new AnimationClock(this.configStore.state.startTime, this.configStore.state.endTime)
        })

        // if we add more and more layers this will not work like this. But think about this later
        this.networkLayer = new NetworkLayer(getNetworkStore())
        this.scene.add(this.networkLayer.sceneObject)
        this.animationLayer = new TrafficAnimationLayer()
        this.scene.add(this.animationLayer.sceneObject)
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
            this.renderSingleFrame()

            if (this.clock) {
                this.clock.advanceTime()
                this.animationLayer.updateTime(this.clock.AnimationTime)
            }
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