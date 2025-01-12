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

// import { Strings, t } from '@apitable/core';
// import React from 'react';
// import { Card } from '../card/card';
// import { IApp } from '../interface';
// import style from '../style.module.less';

// export interface IListDeprecateProps {
//   type: 'open' | 'close';
//   data: IApp[];
// }

// export const List: React.FC<IListDeprecateProps> = ({
//   type,
//   data,
// }) => {
//   return (
//     <div className={style.list}>
//       <div className={style.header}>
//         {type === 'open' ? t(Strings.app_opening) : t(Strings.app_closed)}
//       </div>
//       <div className={style.group}>
//         {data.map((app) => {
//           return (
//             <Card
//               key={app.appId}
//               {...app}
//             />
//           );
//         })}
//       </div>
//     </div >
//   );
// };

import * as React from 'react';
import { Strings, t } from '@apitable/core';
import { Card } from '../card/card';
import { AppStatus, IStoreApp } from '../interface';
import style from '../style.module.less';

export interface IListDeprecateProps {
  type: AppStatus;
  data: IStoreApp[];
}

const ListBase: React.FC<IListDeprecateProps> = ({
  type,
  data,
}) => {
  return (
    <div className={style.list}>
      <div className={style.header}>
        {type === AppStatus.Open ? t(Strings.app_opening) : t(Strings.app_closed)}
      </div>
      <div className={style.group}>
        {data.map((app) => <Card openStatus={type} key={app.appId} {...app} /> )}
      </div>
    </div >
  );
};

export const List = React.memo(ListBase);
