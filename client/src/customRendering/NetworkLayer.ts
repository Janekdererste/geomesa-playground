import Network from "@/Network";
import {BufferAttribute, BufferGeometry, LineBasicMaterial, LineSegments, Mesh} from "three";
import SceneLayer from "@/customRendering/SceneLayer";
import NetworkStore from "@/store/NetworkStore";


export const NETWORK_LAYER = 'network-layer'

export default class NetworkLayer extends SceneLayer {

    constructor(networkStore: NetworkStore) {
        super(NetworkLayer.init())
        networkStore.register(() => this.setNetwork(networkStore.state.network))
    }

    private static init() {
        const attribute = new BufferAttribute(new Float32Array([]), 3)
        const geometry = new BufferGeometry()
        geometry.setAttribute('position', attribute)
        const material = new LineBasicMaterial({color: 0x0000ff})
        const mesh = new LineSegments(geometry, material)
        mesh.name = NETWORK_LAYER
        mesh.position.z = -11
        mesh.frustumCulled = false
        return mesh
    }

    private setNetwork(network: Network) {

        const mesh = this.sceneObject as Mesh
        const geometry = mesh.geometry as BufferGeometry
        geometry.setAttribute('position', new BufferAttribute(network.coords, 3))
    }
}