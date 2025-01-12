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

import { useMount } from 'ahooks';
import { FC, PropsWithChildren, useState } from 'react';
import { CSSTransition } from 'react-transition-group';
import { IconButton } from '@apitable/components';
import { CloseOutlined } from '@apitable/icons';
import styles from './style.module.less';

interface IProps {
  width?: number | string;
  onClose?: () => void;
}

export const Modal: FC<PropsWithChildren<IProps>> = (props) => {
  const { width, children, onClose } = props;

  const [inProp, setInProps] = useState(false);
  useMount(() => {
    setInProps(true);
  });

  return (
    <div className={styles.modelRoot}>
      <CSSTransition
        in={inProp}
        timeout={300}
        classNames='notice-animation'
      >
        <div className={styles.content} style={{ width }}>
          <div className={styles.closeBtnWrap} >
            <IconButton
              className={styles.closeBtn}
              shape='square'
              icon={CloseOutlined}
              onClick={ () => { onClose && onClose(); } }
            />
          </div>
          { children }
        </div>
      </CSSTransition>
    </div>
  );
};
