import Network from "@/Network";
import {BufferAttribute, BufferGeometry, LineBasicMaterial, LineSegments} from "three";
import SceneLayer from "@/SceneLayer";


export const NETWORK_LAYER = 'network-layer'

export default class NetworkLayer extends SceneLayer {

    constructor(network: Network) {
        super(NetworkLayer.init(network))
    }

    private static init(network: Network) {
        const attribute = new BufferAttribute(network.Coords, 3)
        const geometry = new BufferGeometry()
        geometry.setAttribute('position', attribute)
        const material = new LineBasicMaterial({color: 0x0000ff});
        const mesh = new LineSegments(geometry, material);
        mesh.name = NETWORK_LAYER
        mesh.position.z = -11
        mesh.frustumCulled = false
        return mesh
    }
}