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

import cls from 'classnames';
import { FC } from 'react';
import styled from 'styled-components';
import { Button, TextButton } from '@apitable/components';
import { ButtonSize } from 'pc/components/common/button_base';

const StyledButton = styled(TextButton)`
  color: var(--textStaticPrimary) !important;
`;

// import '../style/guide_button.less';

export interface IGuideButton {
  size?: ButtonSize;
  oneBlock?: boolean;
  direction?: 'row' | 'column';
  reversal?: boolean;
  skip?: string;
  prev?: string;
  next?: string;
  onSkip?: () => void;
  onNext?: () => void;
  onPrev?: () => void;
}

// Determine how many buttons are available
export const getButtonNum = (...args: (string | undefined)[][]) => {
  const arr = args[0];
  const btnArrValid = arr.filter(item => item);
  return btnArrValid.length;
};
export const GuideButton: FC<IGuideButton> = props => {
  const { size, oneBlock = false, direction = 'row', next, skip, prev, onSkip, onNext, onPrev } = props;
  const btnNum = getButtonNum([skip, prev, next]);

  if (btnNum === 0) {
    return null;
  }

  const btnClick = (_e: React.MouseEvent, btnText: string | undefined) => {
    if (btnText === next) {
      onNext && onNext();
      return;
    }
    if (btnText === skip) {
      onSkip && onSkip();
      return;
    }
    if (btnText === prev) {
      onPrev && onPrev();
      return;
    }
  };
  const leftBtnText = btnNum === 3 ? prev : prev || skip || next;
  const centerBtnText = skip;
  const rightBtnText = next || skip || prev;

  return (
    <div
      className={'vika-guide-button'}
      style={{
        justifyContent: btnNum === 1 ? 'flex-end' : 'space-between', width: '100%',
        flexDirection: direction === 'column' ? 'column-reverse' : 'row'
      }}
    >
      {btnNum > 1 && (
        <StyledButton
          size={size}
          onClick={e => btnClick(e, leftBtnText)}
          style={direction === 'column' ? { marginTop: '8px' } : {}}
          className={cls('guide-cancel', direction === 'column' ? '' : 'minor-button')}
        >
          {leftBtnText}
        </StyledButton>
      )}
      <div style={{ width: oneBlock && btnNum === 1 ? '100%' : 'auto' }}>
        {btnNum === 3 && (
          <TextButton
            color={'primary'}
            size={size}
            onClick={e => btnClick(e, centerBtnText)}
            block={direction === 'column'}
            className={direction === 'column' ? '' : 'minor-button'}
          >
            {centerBtnText}
          </TextButton>
        )}
        <Button
          color={oneBlock ? 'primary' : ''}
          className={oneBlock ? '' : 'primaryColorBtn'}
          size={size as any}
          onClick={e => btnClick(e, rightBtnText)}
          block={(oneBlock && btnNum === 1) || direction === 'column'}
        >
          {rightBtnText}
        </Button>
      </div>
    </div>
  );
};
