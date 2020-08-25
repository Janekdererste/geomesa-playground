import Api, {Plan, PlanReceived} from "@/API";
import Store from "@/store/Store";
import {Action} from "@/store/Dispatcher";

export interface PlanState {
    selectedPlan: SelectedPlan | undefined
}

export interface SelectedPlan {
    plan: Plan | undefined
    personId: string
    isFetching: boolean
}

export class SelectPerson implements Action {

    constructor(personId: string) {
        this._personId = personId
    }

    private _personId: string

    public get personId() {
        return this._personId
    }
}

export class UnselectPerson implements Action {
}


export default class PlanStore extends Store<PlanState> {

    private api: Api

    constructor(api: Api) {
        super()
        this.api = api
    }

    protected getInitialState(): PlanState {
        return {
            selectedPlan: undefined
        }
    }

    protected reduce(state: PlanState, action: Action): PlanState {

        if (action instanceof PlanReceived) {
            const selectedPlan = {
                plan: action.plan,
                personId: action.personId,
                isFetching: false
            }
            return Object.assign({}, state, {selectedPlan: selectedPlan})
        } else if (action instanceof SelectPerson) {

            this.api.getPlan(action.personId)
            const selectedPlan = {
                plan: undefined,
                personId: action.personId,
                isFetching: true
            }
            return Object.assign({}, state, {selectedPlan: selectedPlan})
        } else if (action instanceof UnselectPerson) {
            return Object.assign({}, state, {selectedPlan: undefined})
        }
        return state
    }
}