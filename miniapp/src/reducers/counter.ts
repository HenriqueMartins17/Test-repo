import { ADD, MINUS } from '../constants/counter';

const INITIAL_STATE = {
  num: 0,
};

export default function counter(state: typeof INITIAL_STATE  = INITIAL_STATE, action: any) {
  switch (action.type) {
    case ADD:
      return {
        ...state,
        num: state.num + 1,
      };
     case MINUS:
       return {
         ...state,
         num: state.num - 1,
       };
     default:
       return state;
  }
}
