import { Assert } from '../types';
import { isEqual, isEqualObject } from './equal';
// import { isExist } from './exist';
import { isInclude } from './include';
import { isCollection } from './collection';
import { isGreaterThan, isLessThan, isGreaterThanOrEqual, isLessThanOrEqual } from './operator';
import { isRange } from './range';

export const ASSERT_FUNCTION = {
  [Assert.Equal]: isEqual,
  [Assert.NotEqual]: (a: string, b: string): boolean => !isEqual(a, b),
  [Assert.EqualObject]: isEqualObject,
  // [Assert.Exist]: isExist,
  // [Assert.NotExist]: (arg: unknown): boolean => !isExist(arg),
  [Assert.Include]: isInclude,
  [Assert.NotInclude]: (content: string, searchValue: string): boolean => !isInclude(content, searchValue),
  [Assert.Collection]: isCollection,
  [Assert.NotCollection]: (content: unknown, collection: unknown[]): boolean => !isCollection(content, collection),
  [Assert.GreaterThan]: isGreaterThan,
  [Assert.LessThan]: isLessThan,
  [Assert.GreaterThanOrEqual]: isGreaterThanOrEqual,
  [Assert.LessThanOrEqual]: isLessThanOrEqual,
  [Assert.Range]: isRange,
  [Assert.NotRange]: (content: string, range: [unknown, unknown] | string): boolean => !isRange(content, range),
};
