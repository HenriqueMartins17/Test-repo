import Decimal from 'decimal.js';

export const isArray = (o: unknown): o is any[] => Array.isArray(o);
export const isBoolean = (o: unknown): o is boolean => typeof o === 'boolean';
export const isNull = (o: unknown): o is null => o === null;
export const isUndefined = (o: unknown): o is undefined => o === undefined;
export const isNullOrUndefined = (val: unknown): boolean => val === undefined || val === null;
export const isString = (o: unknown): o is string => typeof o === 'string';
export const isNumber = (o: unknown): o is number => typeof o === 'number';
export const isObject = (o: unknown): o is Record<string, unknown> => {
  if (o !== undefined && typeof o === 'object' && o !== null) {
    if (o.constructor === undefined) {
      return true;
    }
    if (o.constructor.name === 'Object') {
      return o.constructor === Object || Object.prototype.toString.call(o) === '[object Object]';
    }
  }
  return false;
};

function JSONParse(json: any): any {
  try {
    const o = JSON.parse(json);
    if (isObject(o) || isArray(o) || (isString(o) && o !== json)) {
      return JSONParseRecursion(o);
    }
  } catch (e) {
    if ((e as Error).message.indexOf('token \\ in JSON')) {
      const str = json.replace(/\\\\/g, '\\').replace(/\\"/g, '"');
      if (str !== json) {
        return JSONParse(str);
      }
    }
  }
  return json;
}

export function JSONParseRecursion(obj: unknown): any {
  if (isObject(obj)) {
    const o: Record<string, unknown> = {};
    Object.keys(obj).forEach((key) => {
      o[key] = JSONParseRecursion(obj[key]);
    });
    return o;
  }

  if (isArray(obj)) {
    return obj.map((_, index) => JSONParseRecursion(obj[index]));
  }
  if (isString(obj)) {
    const ret = JSONParse(obj);
    if (isObject(ret) || isArray(ret)) {
      return JSONParseRecursion(ret);
    }
  }
  return obj;
}

export const toString = (value: unknown): string | undefined => {
  if (value === null) {
    return 'null';
  }
  if (value === undefined) {
    return undefined;
  }
  if (typeof value === 'number') {
    return eval(`"${value}"`);
  }
  if (isObject(value) || isArray(value)) {
    try {
      return JSON.stringify(value);
    } catch (e) {
      return value.toString();
    }
  }
  if (Buffer.isBuffer(value)) {
    return value.toString('utf-8');
  }
  return (value as any).toString();
};

export const bigNumber = (val: unknown): Decimal => {
  if (typeof val === 'string' || typeof val === 'number') {
    return new Decimal(val);
  }
  if (typeof val === 'bigint') {
    return new Decimal(val.toString());
  }
  throw new Error('Invalid value');
};
