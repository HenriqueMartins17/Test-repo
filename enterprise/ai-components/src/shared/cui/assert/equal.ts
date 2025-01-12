import { isObject, isArray, toString, JSONParseRecursion } from './utils';

export const isEqual = (a: unknown, b: unknown): boolean => toString(a) === toString(b);

function equalObject(arg0: unknown, arg1: unknown): boolean {
  if (isObject(arg0) && isObject(arg1)) {
    const arr0 = Object.keys(arg0);
    if (arr0.length !== Object.keys(arg1).length) {
      return false;
    }
    return arr0.every((key) => equalObject(arg0[key], arg1[key]));
  }
  if (isArray(arg0) && isArray(arg1)) {
    if (arg0.length !== arg1.length) {
      return false;
    }
    return arg0.every((_, index) => equalObject(arg0[index], arg1[index]));
  }
  return arg0 === arg1;
}

export const isEqualObject = (a: string, b: string): boolean => {
  const json0 = JSONParseRecursion(a);
  const json1 = JSONParseRecursion(b);
  const ret = equalObject(json0, json1);
  return ret;
};
