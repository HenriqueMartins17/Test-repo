import classNames from 'classnames';
import React from 'react';
import { Tooltip } from '@apitable/components';
import styles from './style.module.less';

interface IProps {
  children: React.ReactNode;
  onClick?: () => void;
  className?: string;
  active?: boolean;
  tooltip?: string;
}

export default function ToolBtn(props: IProps) {
  const { tooltip, children, onClick, className, active } = props;
  if (tooltip) {
    return (
      <Tooltip content={tooltip}>
        <div>
          <div
            className={classNames(
              {
                [styles.toolbarIcon]: true,
                [styles.toolbarIconActive]: active,
              },
              className,
            )}
            onClick={onClick}
          >
            {children}
          </div>
        </div>
      </Tooltip>
    );
  }
  return (
    <div
      className={classNames(
        {
          [styles.toolbarIcon]: true,
          [styles.toolbarIconActive]: active,
        },
        className,
      )}
      onClick={onClick}
    >
      {children}
    </div>
  );
}
