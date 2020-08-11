import Api, {Plan, PlanReceived} from "@/API";
import Store from "@/store/Store";
import {Action} from "@/store/Dispatcher";

export interface PlanState {
    selectedPlan: Plan | undefined
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

export default class PlanStore extends Store<PlanState> {

    private api: Api

    constructor(api: Api) {
        super()
        this.api = api
    }

    protected getInitialState(): PlanState {
        return {
            selectedPlan: undefined,
            isFetching: false
        }
    }

    protected reduce(state: PlanState, action: Action): PlanState {

        if (action instanceof PlanReceived) {
            return Object.assign({}, state, {selectedPlan: action.plan, isFetching: false})
        }
        if (action instanceof SelectPerson) {
            this.api.getPlan(action.personId)
            return Object.assign({}, state, {isFetching: true})
        }
        return state
    }
}