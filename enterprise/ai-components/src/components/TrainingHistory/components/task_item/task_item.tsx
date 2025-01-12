import classnames from 'classnames';
import dayjs from 'dayjs';
import * as React from 'react';
import { FC } from 'react';
import { Box, Typography, useThemeColors } from '@apitable/components';
import { ItemStatus } from './components/item_status/item_status';
import { ITrainingTaskItem } from './interface';
import styles from './styles.module.less';

export const CONST_DATETIME_FORMAT = 'YYYY-MM-DD HH:mm:ss';

export const TaskItem: FC<{ activeId?: string; item: ITrainingTaskItem; onClick?: () => void; isSummary?: boolean; hideMoreOperation?: boolean }> = ({
  item,
  isSummary,
  activeId,
  onClick,
  hideMoreOperation,
}) => {
  const colors = useThemeColors();
  const isActive = item.taskId === activeId;

  return (
    <div
      className={classnames('vk-flex vk-flex-row vk-p-2 vk-rounded vk-cursor-pointer', styles.hoverBg)}
      style={{
        background: isActive ? colors.bgBrandLightDefault : '',
      }}
      onClick={onClick}
    >
      <Box display={'flex'} flexDirection={'row'} marginRight={'8px'} paddingTop={'4px'}>
        <ItemStatus status={item.status} variant={'outlined'} />
      </Box>

      <Box flex={'1'}>
        <Box display={'flex'} justifyContent={'space-between'} alignItems={'center'}>
          <Typography variant="body2" color={colors.textCommonPrimary}>
            {dayjs(item.createdAt).format(CONST_DATETIME_FORMAT)}
          </Typography>
        </Box>
      </Box>
    </div>
  );
};
