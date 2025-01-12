import cls from 'classnames';
import * as React from 'react';
import { useContext, useEffect, useState } from 'react';
import { useSelector } from 'react-redux';
import AutoSizer from 'react-virtualized-auto-sizer';
import { FixedSizeList } from 'react-window';
import { Alert, Button, Skeleton } from '@apitable/components';
import { DatasheetApi, IDatasheetTablebundles, t, Strings, Selectors, StoreActions, PREVIEW_DATASHEET_ID } from '@apitable/core';
import { RefreshOutlined } from '@apitable/icons';
import { Message } from 'pc/components/common';
import { SideBarContext } from 'pc/context';
import { useCatalogTreeRequest } from 'pc/hooks';
import { useAppDispatch } from 'pc/hooks/use_app_dispatch';
import { BackupItem } from './backup_item';
import { BackupContext } from './context';
import { IBackup } from './interface';
import { BackupRecoverModal } from './recover_modal';
import styles from './style.module.less';

export const createBackupSnapshot = async (dst: string) => {
  return await DatasheetApi.createDatasheetTablebundle(dst);
};

export const Backup = (props: IBackup) => {
  const { datasheetId, curPreview, setCurPreview } = props;
  const [data, setData] = useState<IDatasheetTablebundles[]>([]);
  const [loading, setLoading] = useState(false);
  const [recoverTbdId, setRecoverTbdId] = useState('');

  const isPending = data.some(d => d.statusCode === 0 && !d.isDeleted);

  const dispatch = useAppDispatch();

  const { newTdbId } = useContext(SideBarContext);
  const { checkNodeNumberLimit } = useCatalogTreeRequest();
  const curDatasheet = useSelector((state) => Selectors.getDatasheet(state, datasheetId));
  const treeNodesMap = useSelector((state) => state.catalogTree.treeNodesMap);
  const type = treeNodesMap[datasheetId]?.type;
  const manageable = curDatasheet?.permissions.manageable;

  const addTablebundles = async (tbdId: string, cb?: () => void) => {
    const tablebundlesRlt = await DatasheetApi.getDatasheetTablebundles(datasheetId, tbdId);
    const newtablebundles = tablebundlesRlt.data.data;
    setData([...newtablebundles, ...data]);
    if (newtablebundles[0]?.statusCode === 0) {
      // processing interval
      setTimeout(() => {
        addTablebundles(tbdId, cb);
      }, 3000);
    } else {
      cb?.();
    }
  };

  useEffect(() => {
    if (newTdbId && data.every(d => d.tbdId !== newTdbId)) {
      addTablebundles(newTdbId);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [newTdbId]);

  const createSnapshot = async () => {
    const res = await createBackupSnapshot(datasheetId);
    if (res.data.success) {
      const newData = res?.data?.data;
      await addTablebundles(newData.tbdId, () => {
        Message.success({
          content: t(Strings.backup_create_success)
        });
      });
    } else {
      const errorData = res.data.data as any;
      if (!errorData) {
        Message.error({
          content: res.data.message
        });
        return;
      }
      const name = t(Strings[errorData.product.toLowerCase()]) || errorData.product;
      Message.error({
        content: t(Strings[res.data.message], {
          product: name,
          count: errorData.count
        })
      });
    }
  };

  useEffect(() => {
    const fetchTablebundles = async () => {
      setLoading(true);
      const res = await DatasheetApi.getDatasheetTablebundles(datasheetId);
      const _data = res?.data?.data;
      setData(_data);
      setLoading(false);
    };

    fetchTablebundles();
  }, [datasheetId]);

  if (loading) {
    return (
      <div className={styles.loading}>
        <Skeleton count={2} />
        <div className={styles.loadingBottom}>
          <Skeleton image circle style={{ width: '20px', height: '20px', marginRight: '8px' }} />
          <Skeleton width='61%' />
        </div>
      </div>
    );
  }
  return (
    <BackupContext.Provider value={{
      data, setData,
      curPreview, setCurPreview,
      recoverTbdId, setRecoverTbdId,
      datasheetId, curDatasheet, type
    }}>
      <div className={styles.backup}>
        <Button
          disabled={isPending || !manageable}
          className={cls(styles.backupCreate, isPending && styles.loading)}
          onClick={createSnapshot}
          prefixIcon={<RefreshOutlined currentColor />}
        >
          {isPending ? t(Strings.backup_creating) : t(Strings.backup_create)}
        </Button>
        <div className={styles.backupList}>
          <div className={styles.backupListLeft}/>
          <AutoSizer style={{ width: '100%', height: '100%' }}>
            {({ height, width }) => (
              <FixedSizeList
                height={height}
                width={width}
                itemCount={data.length}
                itemSize={104}
              >
                {({ index, style }: any) => {
                  return (
                    <div style={{
                      ...style,
                      height: index === data.length - 1 ? '120px' : style.height
                    }} key={data[index].id} className={styles.backupItemWarpper}>
                      <BackupItem {...data[index]}/>
                      {data.length > 0 && index === data.length - 1 && <div className={styles.bottomTip}>
                        {t(Strings.no_more)}
                      </div>}
                    </div>
                  );
                }}
              </FixedSizeList>
            )}
          </AutoSizer>
        </div>
        {recoverTbdId && <BackupRecoverModal setRecoverTbdId={setRecoverTbdId} tbdId={recoverTbdId} />}
        {curPreview && <Alert
          className={styles.backupPreview}
          type="default"
          closable
          content={(
            <div className={styles.previewContent}>
              <span>{t(Strings.preview_time_machine, { version: data.filter(d => d.tbdId === curPreview)?.[0]?.name })}</span>
              <Button
                size="small"
                color="primary"
                onClick={() => {
                  const isLimit = checkNodeNumberLimit(type);
                  if (!isLimit) {
                    setRecoverTbdId(curPreview as string);
                  }
                }}
                disabled={!manageable}
              >{t(Strings.backup_action_recover)}</Button>
            </div>
          )}
          onClose={() => {
            setCurPreview(undefined);
            dispatch(StoreActions.resetDatasheet(PREVIEW_DATASHEET_ID));
          }}
        />}
      </div>
    </BackupContext.Provider>
  );
};
