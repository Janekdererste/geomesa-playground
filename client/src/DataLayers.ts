import {BufferAttribute, BufferGeometry, LineBasicMaterial, LineSegments, Mesh, Scene} from "three";
import Network from "@/Network";

const NETWORK_LAYER = 'network-layer'

export default class DataLayers {

    private scene = new Scene()

    constructor() {
        this.initializeNetworkLayer()
    }

    get Scene() {
        return this.scene
    }

    updateNetworkLayer(network: Network) {

        const networkLayer = this.scene.getObjectByName(NETWORK_LAYER) as Mesh
        const geometry = networkLayer.geometry as BufferGeometry
        const attribute = new BufferAttribute(network.Coords, 3);
        attribute.needsUpdate = true
        geometry.setAttribute('position', attribute)
    }

    private initializeNetworkLayer() {

        const attribute = new BufferAttribute(new Float32Array([0, 0, 0, 10000, 10000, 0]), 3)
        const geometry = new BufferGeometry()
        geometry.setAttribute('position', attribute)
        const material = new LineBasicMaterial({color: 0x0000ff});
        const mesh = new LineSegments(geometry, material);
        mesh.name = NETWORK_LAYER
        mesh.position.z = -11
        mesh.frustumCulled = false
        this.scene.add(mesh)
    }
}