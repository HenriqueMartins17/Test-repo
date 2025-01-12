import { createStore, applyMiddleware, compose } from 'redux';
import thunkMiddleware from 'redux-thunk';
import rootReducer from '../reducers';

const composeEnhancers =
  typeof window === 'object' &&
  (window as any).__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ ?
    (window as any).__REDUX_DEVTOOLS_EXTENSION_COMPOSE__({
      // Specify extension’s options like name, actionsBlacklist, actionsCreators, serialize...
    }) : compose;

const middlewares = [
  thunkMiddleware,
];

declare function require(name: string);
if (process.env.NODE_ENV === 'development' && process.env.TARO_ENV !== 'quickapp') {
  // tslint:disable-next-line: no-var-requires
  middlewares.push(require('redux-logger').createLogger());
}

const enhancer = composeEnhancers(
  applyMiddleware(...middlewares),
  // other store enhancers if any
);

export default function configStore() {
  const store = createStore(rootReducer, enhancer);
  return store;
}
