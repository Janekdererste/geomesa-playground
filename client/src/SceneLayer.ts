import {Object3D} from "three";

export default class SceneLayer {

    constructor(sceneObject: Object3D) {
        this._sceneObject = sceneObject
    }

    _sceneObject: Object3D

    get sceneObject() {
        return this._sceneObject
    }
}