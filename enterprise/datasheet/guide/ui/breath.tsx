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

import { DomEventListener, FocusDom } from '../common/element';
import { IBackdropType } from '../common/interface';
import { destroyAroundMask, showAroundMask } from './around_mask';
import { destroyUnderlyingMask, showUnderlyingMask } from './underlying_mask';

const allClasses = [
  'vika-guide-breath-outset',
  'vika-guide-breath-inset',
  'vika-guide-breath-outset-orange',
  'vika-guide-breath-inset-orange',
];

export interface IGuideBreathOptions {
  element: string;
  shadowDirection?: 'outset' | 'inset' | 'none';
  color?: 'orange';
  onTarget?: () => void;
  backdrop?: IBackdropType;
}

const getBreathClass = (shadowDirection: string, color?: string) => {
  let className = 'vika-guide-breath-' + shadowDirection;
  if (color) className = className + '-' + color;
  return className;
};

let curNode: HTMLElement | null;
let timer: any;
let curOnTarget: (() => void) | undefined;
export const showBreath = (options: IGuideBreathOptions) => {
  const { shadowDirection = 'outset', color, element, onTarget, backdrop } = options;
  let previousNode: HTMLElement | null;

  // Adding a class to the target element
  const add = (targetNode: HTMLElement) => {
    targetNode.classList.add(getBreathClass(shadowDirection, color));
  };

  const remove = (targetNode: HTMLElement) => {
    targetNode.classList.remove(getBreathClass(shadowDirection, color));
  };

  // The dom element was found
  const domFound = (targetDom: HTMLElement) => {
    previousNode = curNode;
    curNode = targetDom;
    if (previousNode === curNode) return;
    curOnTarget = onTarget;
    previousNode && remove(previousNode);
    add(curNode);
    if (backdrop && backdrop === 'underlying_mask') {
      showUnderlyingMask({ eleString: element });
    } else if (backdrop && backdrop === 'around_mask') {
      showAroundMask({ eleString: element });
    }
  };

  const Breath = FocusDom({
    element,
    timer,
    domFound,
    onTarget,
  });

  // @ts-ignore
  const breath = new Breath();
  breath.init();
};

export const destroyBreath = () => {
  const remove = () => {
    if (curNode) {
      allClasses.forEach(className => {
        curNode!.classList.remove(className);
      });
    }
    destroyUnderlyingMask();
    destroyAroundMask();
    if (curOnTarget) {
      DomEventListener.removeEventListener();
      curOnTarget = undefined;
    }
    curNode = null;
    curOnTarget = undefined;
  };
  remove();
};
