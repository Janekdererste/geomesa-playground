import React from 'react'
import {PlanState} from "@/store/PlanStore";
import {Activity, Leg, Plan} from "@/API";

export interface SidebarProps {
    planState: PlanState
}

const isLeg = (element: Activity | Leg): element is Leg => {
    return (element as Leg).mode !== undefined
}

const isActivity = (element: Activity | Leg): element is Activity => {
    return (element as Activity).coordinate !== undefined // this needs probably more checks
}

const getElementLabel = (element: Activity | Leg) => {
    if (isLeg(element)) {
        return element.mode + ' leg'
    } else if (isActivity(element)) {
        return element.type + ' activity (' + element.startTime + " - " + element.endTime + ")"
    }
    return 'upsi'
}

export const Sidebar = (props: SidebarProps) => (<div>

    <h3>Person: {props.planState.selectedPlan?.personId}</h3>

    {props.planState.selectedPlan?.plan ? <Plan plan={props.planState.selectedPlan.plan} /> : <span>Loading...</span>}

</div>)

interface PlanProps { plan: Plan }
const Plan = (props: PlanProps) => (
    <ul>
        {props.plan.elements.map(element => {
            return (<li>{getElementLabel(element)}</li>)
        })}
    </ul>
)


