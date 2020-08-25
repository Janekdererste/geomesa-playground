import SceneLayer from "@/customRendering/SceneLayer";
import {
    BufferAttribute,
    BufferGeometry,
    LineBasicMaterial,
    LineSegments,
    Mesh,
    Object3D,
    Points,
    PointsMaterial
} from "three";
import {Activity, Leg, Plan} from "@/API";
import PlanStore from "@/store/PlanStore";

export const PLAN_LAYER = 'plan-layer'
const ACTIVITIES = 'activities'
const LEGS = 'LEGS'

export default class PlanLayer extends SceneLayer {

    private currentPlan?: Plan

    constructor(planStore: PlanStore) {
        super(PlanLayer.init());
        planStore.register(() => this.setPlan(planStore.state.selectedPlan?.plan))
    }

    private static init() {

        const parent = new Object3D()
        parent.name = PLAN_LAYER
        parent.position.z = -9
        parent.frustumCulled = false

        // lines (legs) of a plan
        const geometry = new BufferGeometry()
        // set attributes here
        const material = new LineBasicMaterial({color: 0xff0000})
        const mesh = new LineSegments(geometry, material)
        mesh.frustumCulled = false
        mesh.name = LEGS
        parent.add(mesh)

        // points (activities) of a plan
        const pointsGeometry = new BufferGeometry()
        const pointMaterial = new PointsMaterial({color: 0xff0000, size: 10})
        const pointMesh = new Points(pointsGeometry, pointMaterial)
        pointMesh.frustumCulled = false
        pointMesh.name = ACTIVITIES
        parent.add(pointMesh)

        return parent
    }

    //TODO: better typequards
    private static isActivity(element: (Activity | Leg)): element is Activity {
        return (element as Activity).coordinate !== undefined
    }

    private static isLeg(element: (Activity | Leg)): element is Leg {
        return (element as Leg).mode !== undefined
    }

    private setPlan(plan?: Plan) {
        if (plan === this.currentPlan) return // don't do anything, if not necessary

        if (plan === undefined) return // unselect things in this case but don't do anything for now

        // get the location of activities
        const flatActivityCoordinates = plan.elements
            .filter(element => PlanLayer.isActivity(element))
            .map(element => element as Activity)
            .map(activity => activity.coordinate)
            .flatMap(coordinate => [coordinate.x, coordinate.y, 0])

        // put the positions into the scene
        const points = this.sceneObject.getObjectByName(ACTIVITIES) as Mesh
        const geometry = points.geometry as BufferGeometry
        geometry.setAttribute('position', new BufferAttribute(new Float32Array(flatActivityCoordinates), 3))

        // get the locations of the legs
        const flatLegCoordinates = plan.elements
            .filter(element => PlanLayer.isLeg(element))
            .map(element => element as Leg)
            .flatMap(leg => {
                return [leg.fromCoordinate.x, leg.fromCoordinate.y, 0, leg.toCoordinate.x, leg.toCoordinate.y, 0]
            })

        // put the positions into the scene
        const lines = this.sceneObject.getObjectByName(LEGS) as Mesh
        const lineGeometry = lines.geometry as BufferGeometry

        //const debugArray = new Float32Array([-15050, -150, 0, 4950, 0,0])
        lineGeometry.setAttribute('position', new BufferAttribute(new Float32Array(flatLegCoordinates), 3))
    }
}