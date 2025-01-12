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

import { UnitTag } from 'pc/components/catalog/permission_settings/permission/select_unit_modal/unit_tag';
import { useSpaceInfo } from 'pc/hooks';
import { getSocialWecomUnitName } from '../../../home';

export interface IAdminNameProps {
  deadline: string;
  mainAdminUserName: string;
  mainAdminUserAvatar: string;
  product: string;
  spaceId: string;
  spaceName: string;
  spaceLogo: string;
  value: string;
  mainAdminUserId?: string;
}

export const AdminName = (props: IAdminNameProps) => {
  const { mainAdminUserAvatar, spaceId, value } = props;
  const { spaceInfo } = useSpaceInfo(spaceId);

  if (!spaceInfo) return null;

  const title = getSocialWecomUnitName({
    name: value,
    isModified: false,
    spaceInfo
  });

  return (
    <UnitTag
      unitId={spaceId}
      avatar={mainAdminUserAvatar}
      name={value}
      title={title}
      deletable={false}
    />
  );
};