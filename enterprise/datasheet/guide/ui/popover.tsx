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

import classNames from 'classnames';
import * as React from 'react';
import { FC, PropsWithChildren, useCallback, useEffect, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { bringInView, DomEventListener, FocusDom, getCalculatedPosition, getFullPageSize, getSize } from '../common/element';
import { IBackdropType } from '../common/interface';
import Position from '../common/position';
import { destroyAroundMask, refreshAroundMask, showAroundMask } from './around_mask';
import { GuideButton, IGuideButton } from './guide_button';
import { destroyUnderlyingMask, showUnderlyingMask } from './underlying_mask';

export type IPlacement =
  | 'top'
  | 'left'
  | 'right'
  | 'bottom'
  | 'topLeft'
  | 'topRight'
  | 'bottomLeft'
  | 'bottomRight'
  | 'leftTop'
  | 'leftBottom'
  | 'rightTop'
  | 'rightBottom'
  | 'midCenter'
  | 'auto'
  | 'leftCenter'
  | 'rightCenter'
  | 'topCenter'
  | 'bottomCenter';

export interface IPopoverBase {
  title?: string;
  description?: string;
  placement?: IPlacement;
  padding?: number;
  offsetX?: number;
  offsetY?: number;
  posInfo?: IPositionInfo;
}

export interface IPopoverOptions extends IPopoverBase {
  targetNode: HTMLElement;
  buttonConfig?: IGuideButton;
  arrowStyle?: React.CSSProperties,
}

export interface IShowPopoverOptions extends IPopoverBase {
  element?: string;
  onTarget?: () => void;
  backdrop?: IBackdropType;
  hidden?: boolean;
}

interface IPositionInfo {
  left: string,
  top: string,
  right: string,
  bottom: string,
  tipNodeClasses: string[],
  transform?: string,
}

let timer: any;
const CLASS_PREFIX = 'vika-guide-popover';
let curOnTarget: (() => void) | undefined;

const Popover: FC<PropsWithChildren<IPopoverOptions>> = (options) => {
  const {
    placement = 'bottomLeft', title, description, children,
    targetNode, offsetX = 0, offsetY = 0, padding = 0, buttonConfig, arrowStyle, posInfo,
  } = options;
  const [positionInfo, setPositionInfo] = useState<IPositionInfo>();

  const getCorrectPosition = useCallback((): IPositionInfo => {
    const elementPosition = getCalculatedPosition(targetNode);
    let info: IPositionInfo;
    switch (placement) {
      case 'left':
      case 'leftTop':
        info = positionOnLeft(elementPosition);
        break;
      case 'leftCenter':
        info = positionOnLeftCenter(elementPosition);
        break;
      case 'leftBottom':
        info = positionOnLeftBottom(elementPosition);
        break;
      case 'right':
      case 'rightTop':
        info = positionOnRight(elementPosition);
        break;
      case 'rightCenter':
        info = positionOnRightCenter(elementPosition);
        break;
      case 'rightBottom':
        info = positionOnRightBottom(elementPosition);
        break;
      case 'top':
      case 'topLeft':
        info = positionOnTop(elementPosition);
        break;
      case 'topCenter':
        info = positionOnTopCenter(elementPosition);
        break;
      case 'topRight':
        info = positionOnTopRight(elementPosition);
        break;
      case 'bottom':
      case 'bottomLeft':
        info = positionOnBottom(elementPosition);
        break;
      case 'bottomCenter':
        info = positionOnBottomCenter(elementPosition);
        break;
      case 'bottomRight':
        info = positionOnBottomRight(elementPosition);
        break;
      case 'midCenter':
        info = positionOnMidCenter(elementPosition);
        break;
      case 'auto':
      default:
        info = autoPosition(elementPosition);
        break;
    }
    return info;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [placement, targetNode, padding, offsetX, offsetY]);

  const fresh = useCallback(() => {
    setPositionInfo(posInfo || getCorrectPosition());
    refreshAroundMask();
  }, [getCorrectPosition, posInfo]);
  useEffect(() => {
    const onResize = () => {
      timer = window.setTimeout(() => {
        fresh();
      }, 50);
    };
    bringInView(targetNode);
    fresh();
    window.addEventListener('resize', onResize, false);
    return () => {
      window.removeEventListener('resize', onResize, false);
      window.clearTimeout(timer);
    };
  }, [getCorrectPosition, targetNode, fresh]);
  
  const positionOnLeft = (elementPosition: Position) => {
    const popoverMargin = padding + 10;
    const pageSize = getFullPageSize();
    const popoverInfo = {
      left: '',
      top: `${(elementPosition.top + offsetY) - padding}px`,
      right: `${pageSize.width - elementPosition.left + popoverMargin}px `,
      bottom: '',
      tipNodeClasses: ['right'],
    };
    return popoverInfo;
  };
  const positionOnLeftBottom = (elementPosition: Position): IPositionInfo => {
    const pageSize = getFullPageSize();
    const popoverMargin = padding + 10;
    const popoverInfo = {
      left: '',
      top: '',
      right: `${pageSize.width - elementPosition.left + popoverMargin}px`,
      bottom: `${pageSize.height - elementPosition.bottom - offsetY}px`,
      tipNodeClasses: ['right', 'position-bottom'],
    };
    return popoverInfo;
  };
  
  const positionOnLeftCenter = (elementPosition: Position): IPositionInfo => {
    const pageSize = getFullPageSize();
    const popoverMargin = padding + 10;
    const elementCenter = (elementPosition.bottom - elementPosition.top) / 2;
    const topCenterPosition = elementPosition.top + elementCenter + offsetX;
    const popoverInfo = {
      left: '',
      top: `${topCenterPosition}px`,
      right: `${pageSize.width - elementPosition.left + popoverMargin + offsetY}px`,
      bottom: '',
      tipNodeClasses: ['right', 'position-center'],
      transform: 'translateY(-50%)',
    };
    return popoverInfo;
  };
  const positionOnRight = (elementPosition: Position): IPositionInfo => {
    const popoverMargin = padding + 10;
    const popoverInfo = {
      left: `${elementPosition.right + popoverMargin}px`,
      top: `${(elementPosition.top + offsetY) - padding}px`,
      right: '',
      bottom: '',
      tipNodeClasses: ['left'],
    };
    return popoverInfo;
  };

  const positionOnRightCenter = (elementPosition: Position): IPositionInfo => {
    const popoverMargin = padding + 10;
    const elementCenter = (elementPosition.bottom - elementPosition.top) / 2;
    const topCenterPosition = elementPosition.top + elementCenter + offsetX;
    const popoverInfo = {
      left: `${elementPosition.right + popoverMargin}px`,
      top: `${topCenterPosition}px`,
      right: '',
      bottom: '',
      tipNodeClasses: ['left', 'position-center'],
      transform: 'translateY(-50%)',
    };
    return popoverInfo;
  };
  const positionOnRightBottom = (elementPosition: Position): IPositionInfo => {
    const popoverMargin = padding + 10;
    const pageSize = getFullPageSize();
    const popoverInfo = {
      left: `${elementPosition.right + popoverMargin}px`,
      top: '',
      right: '',
      bottom: `${pageSize.height - elementPosition.bottom - offsetY}px`,
      tipNodeClasses: ['left', 'position-bottom'],
    };
    return popoverInfo;
  };
  const positionOnTop = (elementPosition: Position): IPositionInfo => {
    const popoverDimensions = getSize(targetNode);
    const popoverHeight = popoverDimensions.height;
    const popoverMargin = padding + 10;
    const popoverInfo = {
      left: `${elementPosition.top - popoverHeight - popoverMargin}px`,
      top: `${(elementPosition.left - padding) + offsetY}px`,
      right: '',
      bottom: '',
      tipNodeClasses: ['bottom'],
    };
    return popoverInfo;
  };
  const positionOnTopCenter = (elementPosition: Position): IPositionInfo => {
    const pageSize = getFullPageSize();
    const { width } = getSize(targetNode);
    const popoverInfo = {
      top: '',
      left: `${elementPosition.left + (width / 2) + offsetX}px`,
      right: '',
      bottom: `${pageSize.height - elementPosition.top - offsetY}px`,
      tipNodeClasses: ['bottom', 'position-center'],
      transform: 'translateX(-50%)',
    };
    return popoverInfo;
  };
  const positionOnTopRight = (elementPosition: Position): IPositionInfo => {
    const pageSize = getFullPageSize();
    const popoverSize = getSize(targetNode);
    const pageHeight = pageSize.height;
    const popoverHeight = popoverSize.height;
    const popoverMargin = padding + 10;
    const pageHeightAfterPopOver = elementPosition.bottom + popoverHeight + popoverMargin;

    // If the height is greater than the remaining height, then use bottom right
    if (pageHeightAfterPopOver >= pageHeight) {
      const popoverInfo = {
        left: '',
        top: '',
        right: `${pageSize.width - elementPosition.right - popoverMargin}px`,
        bottom: `${pageSize.height - elementPosition.top}px`,
        tipNodeClasses: ['bottom', 'position-right'],
      };
      return popoverInfo;
    }
    return positionOnBottomRight(elementPosition);
  };
  const positionOnBottom = (elementPosition: Position): IPositionInfo => {
    const popoverMargin = padding + 10;
    const popoverInfo = {
      left: `${(elementPosition.left - padding) + offsetX}px`,
      top: `${elementPosition.bottom + popoverMargin}px`,
      right: '',
      bottom: '',
      tipNodeClasses: ['top'],
    };
    return popoverInfo;
  };
  const positionOnBottomCenter = (elementPosition: Position): IPositionInfo => {
    const popoverDimensions = getSize(targetNode);
    const popoverWidth = popoverDimensions.width / 2;
    const popoverMargin = padding + 10;
    const nodeCenter = offsetX + elementPosition.left + ((elementPosition.right - elementPosition.left) / 2);
    const popoverInfo = {
      left: `${elementPosition.bottom + popoverMargin}px`,
      top: `${nodeCenter - popoverWidth - padding}px`,
      right: '',
      bottom: '',
      tipNodeClasses: ['top', 'position-center'],
    };
    return popoverInfo;
  };
  const positionOnBottomRight = (elementPosition: Position): IPositionInfo => {
    const pageSize = getFullPageSize();
    const popoverMargin = padding + 10;
    const popoverInfo = {
      left: '',
      top: `${elementPosition.bottom + popoverMargin}px`,
      right: `${pageSize.width - elementPosition.right - popoverMargin}px`,
      bottom: '',
      tipNodeClasses: ['top', 'position-right'],
    };
    return popoverInfo;
  };
  const positionOnMidCenter = (elementPosition: Position): IPositionInfo => {
    const popoverDimensions = getSize(targetNode);
    const popoverHeight = popoverDimensions.height;
    const popoverWidth = popoverDimensions.width / 2;
    const popoverCenter = popoverHeight / 2;

    const elementCenter = (elementPosition.bottom - elementPosition.top) / 2;
    const topCenterPosition = (elementPosition.top - popoverCenter) + elementCenter + offsetY;
    const nodeCenter = offsetX + elementPosition.left + ((elementPosition.right - elementPosition.left) / 2);

    const popoverInfo = {
      left: `${topCenterPosition}px`,
      top: `${nodeCenter - popoverWidth - padding}px`,
      right: '',
      bottom: '',
      tipNodeClasses: ['mid-center'],
    };
    return popoverInfo;
  };
  const autoPosition = (elementPosition: Position): IPositionInfo => {
    const pageSize = getFullPageSize();
    const popoverSize = getSize(targetNode);

    const pageHeight = pageSize.height;
    const popoverHeight = popoverSize.height;
    const popoverMargin = (padding || 0) + 10;

    const pageHeightAfterPopOver = elementPosition.bottom + popoverHeight + popoverMargin;

    // If adding popover would go out of the window height, then show it to the top
    if (pageHeightAfterPopOver >= pageHeight) {
      return positionOnTop(elementPosition);
    }
    return positionOnBottom(elementPosition);
  };
  if (!positionInfo) return null;

  const { left, top, right, bottom, tipNodeClasses, transform = 'none' } = positionInfo;
  return (
    <div className={CLASS_PREFIX} style={{ left, top, right, bottom, transform }}>
      <div className={classNames(`${CLASS_PREFIX}-tip`, tipNodeClasses.join(' '))} style={arrowStyle} />
      <div className={`${CLASS_PREFIX}-title`}>{title}</div>
      <div className={`${CLASS_PREFIX}-description`}>{description}</div>
      {children && <div className={`${CLASS_PREFIX}-children`}>{children}</div>}
      <GuideButton
        {...buttonConfig}
        size='small'
        reversal
      />
    </div>
  );

};

export const showPopover = (options: PropsWithChildren<IShowPopoverOptions>) => {
  const { element, onTarget, backdrop, hidden, ...rest } = options;
  const documentClick = () => {
    if (element && !document.querySelector(element)) {
      destroy();
      return;
    }
    setTimeout(() => {
      if (element && !document.querySelector(element)) {
        destroy();
      }
    }, 500);
  };
  const domFound = (targetNode: any) => {
    setTimeout(() => {
      const div = document.createElement('div');
      document.body.appendChild(div);
      const root = createRoot(div);
      root.render(
        (<Popover {...{ targetNode, eleString: element, ...rest }} />),
      );
      document.addEventListener('click', documentClick);
    });

    // backgrop
    if (backdrop && backdrop === 'underlying_mask' && element) {
      showUnderlyingMask({ eleString: element });
    } else if (backdrop && backdrop === 'around_mask' && element) {
      showAroundMask({ eleString: element });
    }
  };

  const destroy = () => {
    const doms = document.querySelectorAll('.' + CLASS_PREFIX);

    doms.forEach(dom => {
      dom.parentNode && document.body.removeChild(dom.parentNode);
    });

    destroyUnderlyingMask();
    destroyAroundMask();
    if (curOnTarget) {
      DomEventListener.removeEventListener();
      curOnTarget = undefined;
    }
    document.removeEventListener('click', documentClick);
  };

  if (!hidden && element) {
    const PopoverCls = FocusDom({
      timer,
      element,
      domFound,
      onTarget,
    });
    // @ts-ignore
    const popover = new PopoverCls();
    popover.init();
  } else {
    destroy();
  }

};
