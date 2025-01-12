/**
 * APITable <https://github.com/apitable/apitable>
 * Copyright (C) 2022 APITable Ltd. <https://apitable.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import { CopilotProvider } from '@apitable/ai';
import { IconButton, Typography, LinkButton } from '@apitable/components';
import { Selectors, Strings, t } from '@apitable/core';
import { CloseOutlined, WarnCircleFilled } from '@apitable/icons';
import { Logo } from 'pc/components/common/logo';
import { useAppSelector } from 'pc/store/react-redux';
import { CopilotMain } from './main';
import { triggerUsageAlertUniversal } from 'enterprise/billing';
import style from './index.module.less';

export const AGENT_LIST = [
  { label: t(Strings.copilot_help_agent_name), desc: t(Strings.copilot_help_agent_desc), value: 'help' },
  { label: t(Strings.copilot_data_agent_name), desc: t(Strings.copilot_data_agent_desc), value: 'data' },
  // { label: 'Auto Agent', desc: 'List description' },
];

interface ICopilot {
  onClose: (visible: boolean) => void;
}

export const Copilot: React.FC<React.PropsWithChildren<ICopilot>> = ({ onClose }) => {
  const datasheetId = useAppSelector(Selectors.getActiveDatasheetId)!;
  const view = useAppSelector(Selectors.getActiveView);
  const spaceId = useAppSelector((state) => state.space.activeId);

  return (
    <div className={style.copilot}>
      <div className={style.copilotHeader}>
        <div className={style.copilotHeaderTitle}>
          <Typography variant='body2'>Copilot</Typography>
          <Typography className={style.copilotHeaderTitleDesc} variant='body4'>{ view?.name }</Typography>
        </div>
        <IconButton shape="square" onClick={() => onClose(false)} icon={CloseOutlined} />
      </div>
      <div className={style.copilotMain}>
        <CopilotProvider
          spaceId={spaceId as string}
          context={{
            datasheetId: datasheetId,
            viewId: view?.id as string,
          }}
          triggerUsageAlert={() => {
            triggerUsageAlertUniversal(t(Strings.subscribe_credit_usage_over_limit));
          }}
          banner={{
            logo: <Logo size={40} text={false} />,
            title: 'AITable Copilot',
            content: (type: string) => {
              const agent = AGENT_LIST.find((item) => item.value === type);
              return agent?.desc || '';
            }
          }}
          privacy={(
            <div className={style.privacy}>
              <WarnCircleFilled className={style.icon} />

              <Typography variant='body4'>
                {t(Strings.copilot_data_agent_policy)}
              </Typography>
              <Typography variant='body4' style={{ marginLeft: 12 }}>
                <LinkButton
                  href='https://aitable.ai/Copilot-Agreement'
                  target='_blank'
                  underline={false}
                >
                  {t(Strings.copilot_data_agent_policy_button)}
                </LinkButton>
              </Typography>

            </div>
          )}
        >
          <CopilotMain />
        </CopilotProvider>

      </div>
    </div>
  );
};
