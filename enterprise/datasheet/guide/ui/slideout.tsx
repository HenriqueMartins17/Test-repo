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

import { useToggle } from 'ahooks';
import classNames from 'classnames';
import Image from 'next/image';
import * as React from 'react';
import { FC, useEffect } from 'react';
import { createRoot } from 'react-dom/client';
import SuccessImg from 'static/icon/common/common_img_slideout.png';
import { GUIDE_SLIDEOUT_Z_INDEX } from '../common/constant';
import { GuideButton, IGuideButton } from './guide_button';

type IPlacement =
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
  | 'rightBottom';

const TransitionDuration = 400;

export interface IDelayShowProps {
  children?: React.ReactNode;
  style?: React.CSSProperties;
  className?: string;
  show?: boolean;
}

export interface IGuideSlideOutProps {
  title?: string;
  description?: string;
  placement?: IPlacement;
  buttonConfig?: IGuideButton;
  children?: React.ReactNode;
  style?: React.CSSProperties;
  className?: string;
}

const CLASS_PREFIX = 'delayshow';
export const DelayShow: FC<IDelayShowProps> = (options) => {
  const { children, style, className, show: showProps } = options;
  const [show, { set: setShow }] = useToggle(false);
  useEffect(() => {
    if (showProps) {
      setTimeout(() => {
        setShow(true);
      }, 300);
    } else {
      setShow(false);
    }
  });
  return (
    <div
      className={classNames(
        { [CLASS_PREFIX]: true },
        { [CLASS_PREFIX + '-shown']: show },
        className,
      )}
      style={{ transitionDuration: `${TransitionDuration / 1000}s`, ...style }}
    >
      {children}
    </div>
  );
};

export const Slideout: FC<IGuideSlideOutProps> = (options) => {
  const { title, description, placement = 'bottomRight', children, buttonConfig } = options;

  const img = SuccessImg;
  const placementCalc = (placement: IPlacement) => {
    const positionStyle = {};
    if (placement.indexOf('top') >= 0 || placement.indexOf('Top') >= 0) {
      positionStyle['top'] = 32;
    }
    if (placement.indexOf('bottom') >= 0 || placement.indexOf('Bottom') >= 0) {
      positionStyle['bottom'] = 32;
    }
    if (placement.indexOf('left') >= 0 || placement.indexOf('Left') >= 0) {
      positionStyle['left'] = 32;
    }
    if (placement.indexOf('right') >= 0 || placement.indexOf('Right') >= 0) {
      positionStyle['right'] = 32;
    }
    return positionStyle;
  };
  return (
    <DelayShow
      className={classNames('vika-guide-slideout', { [placement]: true })}
      style={{ ...placementCalc(placement), zIndex: GUIDE_SLIDEOUT_Z_INDEX }}
      show
    >
      <div className={'imgWrap'}>
        <span className='img'>
          <Image src={img} alt='vika.cn' />
        </span>
      </div>
      {title && <div className='title'>{title}</div>}
      {
        description &&
        <div className={classNames('description', { descBold: !title })}>{description}</div>
      }
      <div className='content'>
        {children}
        <GuideButton {...buttonConfig} oneBlock direction='column' />
      </div>
    </DelayShow>
  );

};

export const showSlideout = (options: IGuideSlideOutProps) => {

  const render = () => {
    const div = document.createElement('div');
    document.body.appendChild(div);
    const root = createRoot(div);
    root.render(
      (<Slideout {...options} />),
    );
  };

  const run = () => {
    destroySlideout();
    render();
  };

  run();
};

export const destroySlideout = () => {
  const destroy = () => {
    const dom = document.querySelector('.' + CLASS_PREFIX);
    const node = dom && dom.parentNode;
    node && document.body.removeChild(node);
  };
  destroy();
};
