/**
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

export default class Position {
  // /**
  //  * @param {number} left
  //  * @param {number} top
  //  * @param {number} right
  //  * @param {number} bottom
  //  */
  left: number;
  right: number;
  top: number;
  bottom: number;

  constructor({
    left = 0,
    top = 0,
    right = 0,
    bottom = 0,
  } = {}) {
    this.left = left;
    this.right = right;
    this.top = top;
    this.bottom = bottom;
  }

  /**
   * Checks if the position is valid to be highlighted
   * @returns {boolean}
   * @public
   */
  canHighlight() {
    return this.left < this.right && this.top < this.bottom;
  }
}