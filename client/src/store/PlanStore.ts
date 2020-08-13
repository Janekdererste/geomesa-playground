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
            selectedPlan: undefined,
            isFetching: false
        }
    }

    protected reduce(state: PlanState, action: Action): PlanState {

        if (action instanceof PlanReceived) {
            return Object.assign({}, state, {selectedPlan: action.plan, isFetching: false})
        } else if (action instanceof SelectPerson) {
            // this could lead to race conditions if a previously selected agent is still fetched and is received after
            // another person was selected or unselect was triggered. Probably a placeholder loading object, which holds
            // the selected id is the way to go here
            this.api.getPlan(action.personId)
            return Object.assign({}, state, {isFetching: true})
        } else if (action instanceof UnselectPerson) {

            return Object.assign({}, state, {selectedPLan: undefined})
        }
        return state
    }
}