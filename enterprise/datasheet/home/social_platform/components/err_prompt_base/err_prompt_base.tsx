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
import Image, { StaticImageData } from 'next/image';
import { Button } from '@apitable/components';
import { Wrapper } from 'pc/components/common';
import DefaultImg from 'static/icon/common/common_img_feishu_binding.png';
import styles from './style.module.less';

export interface IErrPromptBase {
  headerLogo?: string;
  title?: string;
  img?: React.ReactNode | StaticImageData;
  desc: string;
  btnText: string;
  onClick: () => void;
}

export const ErrPromptBase = (data: IErrPromptBase) => {
  const { img = DefaultImg, desc, btnText, onClick, headerLogo, title } = data;
  return (
    <Wrapper hiddenLogo className="center">
      <div
        className={classNames(
          'commonWrapper',
          'center',
          styles.errPromptBase,
          title && styles.errPromptBaseHasTitle
        )}
      >
        <div className="commonImgWrapper">
          {headerLogo && <Image src={headerLogo} alt="" />}
        </div>
        <span className={styles.mainImg}>
          <Image src={img as string} alt="" />
        </span>
        {title && <div className={styles.title}>{title}</div>}
        <div className={styles.desc}>{desc}</div>
        <Button color="primary" onClick={onClick}>
          {btnText}
        </Button>
      </div>
    </Wrapper>
  );
};

