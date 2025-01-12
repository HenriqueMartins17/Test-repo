import { Col, Row } from 'antd';
import dayjs from 'dayjs';
import { cloneDeep } from 'lodash';
import { useEffect, useMemo, useState } from 'react';
import { Button, getThemeName, IconButton, Message, Modal, ThemeName, Typography, useThemeColors } from '@apitable/components';
import { Strings, t } from '@apitable/core';
import { AddFilled, NewtabOutlined, ToggleOutlined } from '@apitable/icons';
import EmptyPngDark from 'static/image/empty_state_dark.png';
import EmptyPngLight from 'static/image/empty_state_light.png';
import { DrawerWrapper } from '../DrawerWrapper';
import { TrainingHistory } from '../TrainingHistory';
// import { getEnvVariables } from 'pc/utils/env';
import styles from './style.module.less';
import { ConfigItem } from '@/components';
import { DataSourceModal } from '@/components/DataSourceModal';
import { IDataSourceModalProps } from '@/components/DataSourceModal/interface';
import { SourceIcon } from '@/components/Training/components/SourceIcon';
import { SourceType } from '@/components/Training/enum';
import { AIType, IAISourceDatasheet, useAIContext } from '@/shared';

interface ITrainProps {
  visible: boolean;
  close: () => void;
  tabConfig?: IDataSourceModalProps['tabConfig'];
}

interface ICreditCost {
  characters: number;
  creditCost: number;
  words: number;
}

export const Training: React.FC<ITrainProps> = ({ visible, close, tabConfig }) => {
  const color = useThemeColors();
  const { context } = useAIContext();
  const [dataSource, setDataSource] = useState<IAISourceDatasheet[]>(() => cloneDeep(context.data.dataSources) || []);
  const [loading, setLoading] = useState(false);
  const themeName = getThemeName();
  const EmptyFolderImg = themeName === ThemeName.Light ? EmptyPngLight : EmptyPngDark;
  const [dataSourceModalOpen, setDataSourceModalOpen] = useState(false);

  const openNewDatasheetTab = (nodeId: string) => {
    // Router.newTab(Navigation.WORKBENCH, { params: { nodeId } });
  };

  const isChangedDataSheet = useMemo(() => {
    if (!context.data.dataSources && !dataSource.length) return false;

    if (
      context.data.dataSources &&
      (dataSource[0]?.nodeId !== context.data.dataSources[0]?.nodeId ||
        dataSource[0]?.setting?.viewId !== context.data.dataSources[0]?.setting?.viewId)
    ) {
      return true;
    }

    if (!context.data.dataSources && dataSource.length) return true;

    return false;
  }, [dataSource, context.data]);

  const _hide = () => {
    if (isChangedDataSheet) {
      Modal.confirm({
        title: t(Strings.ai_close_setting_tip_title),
        content: t(Strings.ai_close_setting_tip_content),
        onOk: () => {
          close();
        },
        okText: t(Strings.ai_discard_setting_edit_ok_text),
        cancelText: t(Strings.ai_discard_setting_edit_cancel_text),
      });
      return;
    }
    close();
  };

  const onSubmit = async () => {
    try {
      setLoading(true);
      await context.api.updateAIInfo({ dataSources: dataSource }, context.data.id);
      if (isChangedDataSheet) {
        await context.api.train(context.data.id);
        context.clearCurrentConversation();
      }
      await context.fetchAIDetail(true, isChangedDataSheet);
      // close();
      Message.success({ content: t(Strings.ai_update_setting_success) });
    } catch (e: any) {
      if (e.errorFields) {
        Message.error({ content: e.errorFields[0].errors[0] });
      } else {
        Message.error({ content: e.message });
      }
    } finally {
      setLoading(false);
    }
  };

  const onRetrain = async () => {
    try {
      setLoading(true);
      await context.api.train(context.data.id);
      context.clearCurrentConversation();
      await context.fetchAIDetail(true, true);
      // close();
    } catch (e: any) {
      Message.error({ content: (e as Error).message });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (visible) {
      const dataSources = cloneDeep(context.data.dataSources) || [];
      setDataSource(dataSources);
    }
  }, [visible]);

  const isQAType = context.data.setting.type === AIType.Qa;

  const onClick = () => {
    setDataSourceModalOpen(true);
  };

  return (
    <DrawerWrapper
      open={visible}
      close={_hide}
      config={{ title: t(Strings.ai_toolbar_training), documentLink: '', modalWidth: 930 }}
      classNames={styles.drawerWrapper}
    >
      {isQAType ? (
        <>
          <Typography variant={'body3'} color={color.textCommonTertiary} className={'!vk-mb-6'}>
            {t(Strings.ai_training_page_desc)}
          </Typography>
          <ConfigItem
            configTitle={t(Strings.ai_robot_data_source_title)}
            description={
              // 只有之前是QA模式 并且有数据源 并且数据源没有变化
              context.data.type === AIType.Qa && dataSource.length && !isChangedDataSheet
                ? t(Strings.ai_latest_train_date, { date: dayjs(context.data.latestTrainingCompletedAt).format('YYYY-MM-DD hh:mm:ss') })
                : ''
            }
          >
            {dataSource.length ? (
              <>
                <Row className={styles.selectDstItem}>
                  <Col className={styles.nodeWrapper}>{<SourceIcon type={dataSource[0].nodeType as unknown as SourceType} />}</Col>
                  <Col flex={1}>
                    <div style={{ display: 'flex', alignItems: 'center' }}>
                      <Typography
                        variant={'body4'}
                        color={color.textCommonPrimary}
                        ellipsis
                        style={{ maxWidth: context.data.dataSourcesUpdated ? 330 : 420 }}
                      >
                        {dataSource[0]?.nodeName}
                      </Typography>
                      {context.data.dataSourcesUpdated && <span className={styles.updateAvailable}>{t(Strings.ai_data_source_update)}</span>}
                    </div>
                    <Typography variant={'body4'} color={color.textCommonTertiary}>
                      {t(Strings.ai_data_source_rows, { rows: dataSource[0]?.setting?.rows })}
                    </Typography>
                  </Col>
                  <Col style={{ display: 'flex', alignItems: 'center', marginRight: 8 }}>
                    <IconButton icon={NewtabOutlined} onClick={() => openNewDatasheetTab(dataSource[0].nodeId)} />
                  </Col>
                  <Col style={{ display: 'flex', alignItems: 'center' }}>
                    <IconButton onClick={onClick} icon={ToggleOutlined} />
                  </Col>
                </Row>
                <div>
                  {(context.data.dataSourcesUpdated || isChangedDataSheet) && (
                    <Button
                      loading={loading}
                      size="small"
                      style={{ marginRight: 8 }}
                      color={'primary'}
                      onClick={isChangedDataSheet ? onSubmit : onRetrain}
                    >
                      {isChangedDataSheet ? t(Strings.ai_save_and_train) : t(Strings.ai_retrain)}
                    </Button>
                  )}
                </div>
              </>
            ) : (
              <div className={styles.selectDst} onClick={onClick}>
                <AddFilled />
                <Typography style={{ marginLeft: 8 }} color={color.textCommonTertiary} variant="body3">
                  {t(Strings.ai_select_data_source)}
                </Typography>
              </div>
            )}
          </ConfigItem>
          <DataSourceModal open={dataSourceModalOpen} setOpen={setDataSourceModalOpen} tabConfig={tabConfig} setDataSource={setDataSource} />
          <TrainingHistory aiId={context.data.id} />
        </>
      ) : (
        <div className={'vk-flex vk-flex-col vk-justify-center vk-items-center vk-h-full'}>
          <img src={EmptyFolderImg} alt="" width={320} height={248} />
          <Typography className={'!vk-mt-6 !vk-mb-2'} variant={'h6'}>
            {t(Strings.ai_training_empty_title)}
          </Typography>
          <Typography variant={'body2'}>{t(Strings.ai_training_empty_desc)}</Typography>
        </div>
      )}
    </DrawerWrapper>
  );
};
