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

import { Typography } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import { useSpaceInfo } from 'pc/hooks';
import { getSocialWecomUnitName } from '../../../home';
import styles from './style.module.less';

interface IAdminChangeModalProps {
  spaceId: string;
  mainAdminUserName: string;
  memberName: string;
}
export const AdminChangeModal = (props: IAdminChangeModalProps) => {
  const { mainAdminUserName, memberName, spaceId } = props;
  const { spaceInfo } = useSpaceInfo(spaceId);

  if (!spaceInfo) return null;

  const primaryTitle = getSocialWecomUnitName({
    name: mainAdminUserName,
    isModified: false,
    spaceInfo
  });

  const title = getSocialWecomUnitName({
    name: memberName,
    isModified: false,
    spaceInfo
  });

  return (
    <div>
      <ul>
        <li>
          <Typography className={styles.name} variant="h5">
            {t(Strings.current_main_admin)}：{primaryTitle || t(Strings.empty)}
          </Typography>
        </li>
      </ul>
      <Typography className={styles.name} variant="body3">
        {t(Strings.to_old_main_admin_tip_after_change)}
      </Typography>
      <ul>
        <li>
          <Typography className={styles.name} variant="h5">
            {t(Strings.new_mian_admin)}：{ title }
          </Typography>
        </li>
      </ul>
      <Typography className={styles.name} variant="body3">
        {t(Strings.to_new_main_admin_tip_after_change)}
      </Typography>
    </div>
  );
};