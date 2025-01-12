import { IAISourceDatasheet, ITabCommonProps } from '@apitable/ai';
import { Typography, useThemeColors } from '@apitable/components';
import { AddOutlined, QuestionCircleOutlined } from '@apitable/icons';
import { SecondConfirmType } from 'pc/components/datasheet_search_panel';
import { useAppSelector } from 'pc/store/react-redux';
import { SelectDataSheet } from 'enterprise/chat/main/tool_bar/setting/select_datasheet';
import styles from './style.module.less';

export const DatasheetTab:React.FC<ITabCommonProps> = ({ setDataSource, setOpen }) => {
  const colors = useThemeColors();
  const rootId = useAppSelector((state) => state.catalogTree.rootId);
  const changeDatasheetSource = (value: IAISourceDatasheet[]) => {
    setDataSource(value);
    setOpen(false);
  };

  return (
    <div>
      <div className={styles.tabItemHeader}>
        <div className={styles.headerTitle}>
          <div>Datasheet</div>
          <QuestionCircleOutlined size={12} color={colors.textCommonPrimary} />
        </div>
        <div className={styles.headerDescription}>
          To train an Agent using your table on Airtable, so that the Agent can answer questions related to your Airtable table
        </div>
      </div>
      <div className={'vk-pt-6 vk-space-y-1'}>
        <Typography variant={'body2'}>Select a datasheet as dataset</Typography>
        <SelectDataSheet rootId={rootId} onChange={changeDatasheetSource} secondConfirmType={SecondConfirmType.Chat}>
          {(onClick) => {
            return (
              <div
                className={'vk-flex vk-items-center vk-py-3 vk-px-2 vk-space-x-1 vk-rounded'}
                style={{
                  backgroundColor: colors.bgControlsDefault,
                }}
                onClick={onClick}
              >
                <AddOutlined />
                <Typography variant={'body3'} style={{ color: colors.textCommonQuaternary }}>
                  Click here to select datasheets
                </Typography>
              </div>
            );
          }}
        </SelectDataSheet>
      </div>
    </div>
  );
};
