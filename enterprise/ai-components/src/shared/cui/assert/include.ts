import { isString, toString } from './utils';

export const isInclude = (content: string, searchValue: string): boolean => {
  if (searchValue === '') return false;
  if (isString(content) && isString(searchValue)) {
    return content.indexOf(searchValue) !== -1;
  }
  let string1, string2;
  if (typeof content !== 'string') {
    string1 = toString(content);
    if (string1 === undefined) return false;
  } else {
    string1 = content;
  }
  if (typeof searchValue !== 'string') {
    string2 = toString(searchValue);
    if (string2 === undefined) return false;
  } else {
    string2 = searchValue;
  }
  return string1.indexOf(string2) !== -1;
};
