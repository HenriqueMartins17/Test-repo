import { Col, Row } from 'antd';
import dayjs from 'dayjs';
import { cloneDeep } from 'lodash';
import { useEffect, useMemo, useState } from 'react';
import { useSelector } from 'react-redux';
import { AIType, DrawerWrapper, IAISourceDatasheet, useAIContext } from '@apitable/ai';
import { Button, IconButton, Modal, ThemeName, Typography, useThemeColors } from '@apitable/components';
import { ConfigConstant, Navigation, Strings, t } from '@apitable/core';
import { AddFilled, NewtabOutlined, ToggleOutlined } from '@apitable/icons';
import { getNodeIcon } from 'pc/components/catalog/tree/node_icon';
import { Message } from 'pc/components/common';
import { SecondConfirmType } from 'pc/components/datasheet_search_panel';
import { Router } from 'pc/components/route_manager/router';
import { getEnvVariables } from 'pc/utils/env';
import EmptyPngDark from 'static/icon/datasheet/empty_state_dark.png';
import EmptyPngLight from 'static/icon/datasheet/empty_state_light.png';
import { History } from './history';
import { ConfigItem } from 'enterprise/chat/main/tool_bar/config_item/config_item';
import { SelectDataSheet } from 'enterprise/chat/main/tool_bar/setting/select_datasheet';
import styles from './style.module.less';

interface ITrainProps {
  visible: boolean;
  close: () => void;
}

interface ICreditCost {
  characters: number;
  creditCost: number;
  words: number;
}

export const Train: React.FC<ITrainProps> = ({ visible, close }) => {
  const color = useThemeColors();
  const { context } = useAIContext();
  const [ds, setDS] = useState<IAISourceDatasheet[]>(() => cloneDeep(context.data.dataSources) || []);
  const [predicting, setPredicting] = useState<ICreditCost | true | null | string>(true);
  const rootId = useSelector((state) => state.catalogTree.rootId);
  const [loading, setLoading] = useState(false);
  const themeName = useSelector((state) => state.theme);
  const EmptyFolderImg = themeName === ThemeName.Light ? EmptyPngLight : EmptyPngDark;
  const openNewDatasheetTab = (nodeId: string) => {
    Router.newTab(Navigation.WORKBENCH, { params: { nodeId } });
  };

  const isChangedDataSheet = useMemo(() => {
    if (!context.data.dataSources && !ds.length) return false;

    if (
      context.data.dataSources
      && (
        ds[0]?.nodeId !== context.data.dataSources[0]?.nodeId ||
        ds[0]?.setting?.viewId !== context.data.dataSources[0]?.setting?.viewId
      )
    ) {
      return true;
    }

    if (!context.data.dataSources && ds.length) return true;

    return false;
  }, [ds, context.data]);

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

  const trainPredict = async (dataSources?: IAISourceDatasheet[]) => {
    try {
      setPredicting(true);
      const values = dataSources || ds;
      if (values[0]) {
        const ret = await context.api.trainPredict(context.data.id, {
          dataSources: [{ nodeId: values[0].nodeId }],
        });
        setPredicting(ret.data);
      }
    } catch (e: any) {
      setPredicting(e.message);
    }
  };

  useEffect(() => {
    if (visible && context.data.dataSourcesUpdated) {
      trainPredict();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [context.data.dataSourcesUpdated, visible]);

  const onSubmit = async () => {
    try {
      setLoading(true);
      await context.api.updateAIInfo({ dataSources: ds }, context.data.id);
      if (isChangedDataSheet) {
        await context.api.train(context.data.id);
        context.clearCurrentConversation();
      }
      await context.fetchAIDetail(true, isChangedDataSheet);
      close();
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
      close();
    } catch (e: any) {
      Message.error({ content: (e as Error).message });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (visible) {
      const dataSources = cloneDeep(context.data.dataSources) || [];
      setDS(dataSources);
    } else {
      setPredicting(true);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [visible]);

  const isQAType = context.data.type === AIType.Qa;
  const changeDatasheetSource = (value: IAISourceDatasheet[]) => {
    if (!isQAType) return;
    const dst = value[0];

    if (dst.nodeId === ds[0]?.nodeId && dst.setting?.viewId === ds[0]?.setting?.viewId) return;
    console.log({ value });
    // setDS((preConfig) => ({
    //   ...preConfig,
    //   dataSources: value,
    // }));
    setDS(value);
    trainPredict(value);
  };

  return (
    <DrawerWrapper
      open={visible}
      close={_hide}
      config={{ title: t(Strings.ai_toolbar_training), documentLink: getEnvVariables().AI_TRAINING_HELP_DOC_LINK, modalWidth: 930 }}
      classNames={styles.drawerWrapper}
    >
      {isQAType ? (
        <>
          <Typography variant={'body3'} color={color.textCommonTertiary} className={'!vk-mb-6'}>
            {
              t(Strings.ai_training_page_desc)
            }
          </Typography>
          <ConfigItem
            configTitle={t(Strings.ai_robot_data_source_title)}
            description={
              // 只有之前是QA模式 并且有数据源 并且数据源没有变化
              context.data.type === AIType.Qa && ds.length && !isChangedDataSheet
                ? t(Strings.ai_latest_train_date, { date: dayjs(context.data.latestTrainingCompletedAt).format('YYYY-MM-DD hh:mm:ss') })
                : ''
            }
          >
            {ds.length ? (
              <>
                <Row className={styles.selectDstItem}>
                  <Col className={styles.nodeWrapper}>{getNodeIcon(undefined, ConfigConstant.NodeType.DATASHEET)}</Col>
                  <Col flex={1}>
                    <div style={{ display: 'flex', alignItems: 'center' }}>
                      <Typography
                        variant={'body4'}
                        color={color.textCommonPrimary}
                        ellipsis
                        style={{ maxWidth: context.data.dataSourcesUpdated ? 330 : 420 }}
                      >
                        {ds[0]?.nodeName}
                      </Typography>
                      {context.data.dataSourcesUpdated &&
                        <span className={styles.updateAvailable}>{t(Strings.ai_data_source_update)}</span>}
                    </div>
                    <Typography variant={'body4'} color={color.textCommonTertiary}>
                      {t(Strings.ai_data_source_rows, { rows: ds[0]?.setting?.rows })}
                    </Typography>
                  </Col>
                  <Col style={{ display: 'flex', alignItems: 'center', marginRight: 8 }}>
                    <IconButton icon={NewtabOutlined}
                      onClick={() => openNewDatasheetTab(ds[0].nodeId)} />
                  </Col>
                  <Col style={{ display: 'flex', alignItems: 'center' }}>
                    <SelectDataSheet value={ds} onChange={changeDatasheetSource} rootId={rootId}
                      secondConfirmType={SecondConfirmType.Chat}>
                      {(onClick) => <IconButton onClick={onClick} icon={ToggleOutlined} />}
                    </SelectDataSheet>
                  </Col>
                </Row>
                <div>
                  {typeof predicting === 'object' && predicting?.characters && (
                    <Typography variant={'body4'} color={color.textCommonTertiary} className={'!vk-my-1'}>
                      {t(Strings.ai_train_credit_cost, { words: predicting.characters, credit: predicting.creditCost })}
                    </Typography>
                  )}
                  {(context.data.dataSourcesUpdated || isChangedDataSheet) && (
                    <Button loading={loading || predicting === true} size="small" style={{ marginRight: 8 }}
                      color={'primary'} onClick={isChangedDataSheet ? onSubmit : onRetrain}>
                      {isChangedDataSheet ? t(Strings.ai_save_and_train) : t(Strings.ai_retrain)}
                    </Button>
                  )}
                  {typeof predicting === 'string' && (
                    <Typography variant={'body4'} color={color.textDangerDefault} component={'span'}>
                      {predicting}
                    </Typography>
                  )}
                </div>
              </>
            ) : (
              <SelectDataSheet rootId={rootId} onChange={changeDatasheetSource}
                secondConfirmType={SecondConfirmType.Chat}>
                {(onClick) => (
                  <div className={styles.selectDst} onClick={onClick}>
                    <AddFilled />
                    <Typography style={{ marginLeft: 8 }} color={color.textCommonTertiary} variant="body3">
                      {t(Strings.ai_select_data_source)}
                    </Typography>
                  </div>
                )}
              </SelectDataSheet>
            )}
          </ConfigItem>
          <History aiId={context.data.id} />
        </>
      ) : (
        <div className={'vk-flex vk-flex-col vk-justify-center vk-items-center vk-h-full'}>
          <img src={EmptyFolderImg.src} alt="" width={320} height={248} />
          <Typography className={'!vk-mt-6 !vk-mb-2'} variant={'h6'}>
            {t(Strings.ai_training_empty_title)}
          </Typography>
          <Typography variant={'body2'}>
            {t(Strings.ai_training_empty_desc)}
          </Typography>
        </div>
      )}
    </DrawerWrapper>
  );
};
