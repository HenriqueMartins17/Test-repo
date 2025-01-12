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

import { FC } from 'react';

export enum WecomOpenDataType {
  UserName = 'userName',
  DepartmentName = 'departmentName'
}

interface IWecomOpenDataProps {
  type?: WecomOpenDataType;
  openId: string | undefined;
}

export const WecomOpenData: FC<IWecomOpenDataProps> = (props) => {
  const { 
    type = WecomOpenDataType.UserName,
    openId = '',
  } = props;

  return (
    <ww-open-data
      type={type} 
      openid={openId}
    />
  );
};