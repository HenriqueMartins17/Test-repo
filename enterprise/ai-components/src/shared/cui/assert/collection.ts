import { isArray } from './utils';
import { isEqual } from './equal';

export const isCollection = (content: unknown, collection: unknown[]): boolean => {
  const obj: unknown[] = isArray(collection) ? collection : JSON.parse(collection);
  return obj.some((item: unknown) => isEqual(content, item));
};
