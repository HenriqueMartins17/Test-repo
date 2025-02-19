import { isNullOrUndefined } from './utils';

/**
 * is not null or undefined
 * empty string is not applicable
 * @param content
 */
export const isExist = (content: unknown): boolean => !isNullOrUndefined(content) && content !== '';
