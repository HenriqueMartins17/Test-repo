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
import { motion } from 'framer-motion';
import { useRef, useState, FC, PropsWithChildren } from 'react';
import styles from './style.module.less';

interface ITouchMove {
  id: string;
  initPosition: {
    left: string;
    top: string;
  };
  sessionStorageKey: string;
  onResize?: () => void;
  onClick?: (e: any) => void;
  onDragStart?: () => void;
}

export const TouchMove: FC<PropsWithChildren<ITouchMove>> = (props) => {
  const constraintsRef = useRef(document.body);
  const [stopClick, setStopClick]= useState(false);
  const onDragStart = () => {
    setStopClick(true);
    props.onDragStart && props.onDragStart();
  };
  const onDragEnd = () => {
  };
  const onMouseDown = () => {
    setStopClick(false);
  };

  const onClick = (e: React.MouseEvent) => {
    if(stopClick) return;
    props.onClick && props.onClick(e);
  };
  return (
    <motion.div
      className={classNames(styles.touchMove)}
      drag
      dragConstraints={constraintsRef}
      dragMomentum={false}
      id={props.id}
      onMouseDown={onMouseDown}
      onDragStart={onDragStart}
      onDragEnd={onDragEnd}
      onClick={onClick}
    >
      {props.children }
    </motion.div>
  );
};
