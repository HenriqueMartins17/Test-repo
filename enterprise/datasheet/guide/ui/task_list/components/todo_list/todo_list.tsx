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
import { FC } from 'react';
import * as React from 'react';
import { blackBlue, deepPurple } from '@apitable/components';
import { CheckOutlined } from '@apitable/icons';
import styles from './style.module.less';

export enum TodoState {
  Empty = 'Empty',
  Active = 'Active',
  Done = 'Done',
}

export interface ITodoItem {
  text: string,
  stopEvents?: string[], // Add a blocking default event to the specified todo item
  state: TodoState,
}

interface IProps {
  list: ITodoItem[],
  goAndReset: (index: number) => void;
}

const Radio: FC<{state: TodoState}> = ({ state }) => {
  if (state === TodoState.Empty) {
    return (
      <div className={styles.radio} style={{ borderColor: blackBlue[200] }} />
    );
  }
  if (state === TodoState.Active) {
    return (
      <div className={styles.radio} style={{ borderColor: deepPurple[500] }} />
    ); 
  }
  if (state === TodoState.Done) {
    return (
      <div className={styles.radio} style={{ borderColor: deepPurple[500], backgroundColor: deepPurple[500] }}>
        <div style={{ transform: 'scale(1.2)' }}>
          <CheckOutlined color='#fff' size={17} />
        </div>
      </div>
    ); 
  }
  return null;
};

const stopEvent = (e: React.UIEvent) => {
  e.preventDefault();
};

export const TodoList: FC<IProps> = (props) => {
  return (
    <div>
      {
        props.list.map((item, index) => {
          const stopEventList = (item.stopEvents || []).reduce((events, e) => {
            events[e] = stopEvent;
            return events;
          }, {});
          return (
            <div 
              className={styles.item} 
              key={index}
              {...stopEventList}
              onClick={() => { item.state !== TodoState.Active && props.goAndReset(index); }}
            >
              <Radio state={item.state} />
              <div 
                className={
                  classNames(styles.text, {
                    [styles.active]: item.state === TodoState.Active,
                    [styles.done]: item.state === TodoState.Done,
                  })
                }
              >
                {item.text}
              </div> 
            </div>
          );
        })
      }
    </div>
  );
};
