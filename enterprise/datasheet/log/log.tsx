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

import { useRequest } from 'ahooks';
import { Input, Select as AntSelect, Table, Tag } from 'antd';
import generatePicker from 'antd/es/date-picker/generatePicker';
import classnames from 'classnames';
import dayjs, { Dayjs } from 'dayjs';
import localeData from 'dayjs/plugin/localeData';
import timezone from 'dayjs/plugin/timezone';
import utc from 'dayjs/plugin/utc';
import dayjsGenerateConfig from 'rc-picker/lib/generate/dayjs';
import React, { useEffect, useMemo, useState } from 'react';
import { shallowEqual, useSelector } from 'react-redux';
import { Button, IconButton } from '@apitable/components';
import {
  Api, ConfigConstant, getLanguage, IMember, IReduxState, MemberType, Strings, SystemConfig, t, UnitItem,
} from '@apitable/core';
import { Audit } from '@apitable/core/src/config/system_config.interface';
import { ReloadOutlined, SearchOutlined } from '@apitable/icons';
import { expandUnitModal, SelectUnitSource } from 'pc/components/catalog/permission_settings/permission/select_unit_modal';
import { MemberItem } from 'pc/components/multi_grid/cell/cell_member/member_item';
import { LocalFormat } from 'pc/components/tool_bar/view_filter/filter_value/filter_date/local_format';
import { getSocialWecomUnitName, isSocialWecom } from '../home';
import { Trial } from './trial';

import styles from './styles.module.less';

dayjs.extend(utc);
dayjs.extend(timezone);
dayjs.extend(localeData);
const DatePicker = generatePicker<Dayjs>(dayjsGenerateConfig as any);

interface ILogSearchState {
  dates: [Dayjs | null, Dayjs | null] | null;
  actions: string[];
  keyword: string;
}

interface IAuditGroupOption {
  label: string;
  value: string;
}

interface ILogTablePagination {
  pageNum: number;
  pageSize: number;
}

const auditTypeMapToList = (auditTypeMap: Audit): IAuditGroupOption[] => Object
  .entries(auditTypeMap)
  .filter(([, v]) => v?.online && v?.show_in_audit_log)
  .sort(([, v1], [, v2]) => v1?.sort - v2?.sort)
  .map(([k, v]) => ({
    value: k,
    label: t(Strings[v.name]),
  }));

const ActionToTemplate = ({ record }: any) => {
  const auditConfig = SystemConfig.audit[record.action];

  if (!auditConfig) return <></>;

  switch (auditConfig.name) {
    case SystemConfig.audit.enable_node_share.name:
      return (
        <span>
          {t(Strings.audit_enable_node_share_detail_start, {
            nodeType: ConfigConstant.nodeNameMap.get(record.body.node.nodeType),
          })}
          <a href={`/workbench/${record.body.node.nodeId}`} className="auditSourceLink" target="_self">
            {record.body.node.currentNodeName}
          </a>
          {t(Strings.audit_enable_node_share_detail_end)}
        </span>
      );
    case SystemConfig.audit.disable_node_share.name:
      return (
        <span>
          {t(Strings.audit_disable_node_share_detail_start, {
            nodeType: ConfigConstant.nodeNameMap.get(record.body.node.nodeType),
          })}
          <a href={`/workbench/${record.body.node.nodeId}`} className="auditSourceLink" target="_self">
            {record.body.node.currentNodeName}
          </a>
          {t(Strings.audit_disable_node_share_detail_end)}
        </span>
      );
    case SystemConfig.audit.update_node_share_setting.name:
      return (
        <span>
          {t(Strings.audit_update_node_share_setting_detail_start, {
            nodeType: ConfigConstant.nodeNameMap.get(record.body.node.nodeType),
          })}
          <a href={`/workbench/${record.body.node.nodeId}`} className="auditSourceLink" target="_self">
            {record.body.node.currentNodeName}
          </a>
          {t(Strings.audit_update_node_share_setting_detail_end)}
        </span>
      );
    case SystemConfig.audit.create_node.name:
      return (
        <span>
          {t(Strings.audit_space_node_create_detail_start, {
            nodeType: ConfigConstant.nodeNameMap.get(record.body.node.nodeType),
          })}
          <a href={`/workbench/${record.body.node.nodeId}`} className="auditSourceLink" target="_self">
            {record.body.node.currentNodeName}
          </a>
        </span>
      );
    case SystemConfig.audit.recover_rubbish_node.name:
      return (
        <span>
          {t(Strings.audit_space_rubbish_node_recover_detail_start, {
            nodeType: ConfigConstant.nodeNameMap.get(record.body.node.nodeType),
          })}
          <a href={`/workbench/${record.body.node.nodeId}`} className="auditSourceLink" target="_self">
            {record.body.node.currentNodeName}
          </a>
          {t(Strings.audit_space_rubbish_node_recover_detail_end)}
        </span>
      );
    case SystemConfig.audit.rename_node.name:
      return (
        <span>
          {t(Strings.audit_space_node_rename_detail_start, {
            nodeType: ConfigConstant.nodeNameMap.get(record.body.node.nodeType),
            oldNodeName: record.body.node.oldNodeName,
          })}
          <a href={`/workbench/${record.body.node.nodeId}`} className="auditSourceLink" target="_self">
            {record.body.node.currentNodeName}
          </a>
        </span>
      );
    case SystemConfig.audit.import_node.name:
      return (
        <span>
          {t(Strings.audit_space_node_import_detail_start)}
          <a href={`/workbench/${record.body.node.nodeId}`} className="auditSourceLink" target="_self">
            {record.body.node.currentNodeName}
          </a>
        </span>
      );
    case SystemConfig.audit.copy_node.name:
      return (
        <span>
          {t(Strings.audit_space_node_copy_detail_start, {
            nodeType: ConfigConstant.nodeNameMap.get(record.body.node.nodeType),
            sourceNodeName: record.body.node.sourceNodeName,
          })}
          <a href={`/workbench/${record.body.node.nodeId}`} className="auditSourceLink" target="_self">
            {record.body.node.currentNodeName}
          </a>
        </span>
      );
    case SystemConfig.audit.move_node.name:
      return (
        <span>
          {t(Strings.audit_space_node_move_detail_start, {
            nodeType: ConfigConstant.nodeNameMap.get(record.body.node.nodeType),
          })}
          <a href={`/workbench/${record.body.node.nodeId}`} className="auditSourceLink" target="_self">
            {record.body.node.currentNodeName}
          </a>
          {t(Strings.audit_space_node_move_detail_end, {
            parentName: record.body.node.parentName,
          })}
        </span>
      );
    case SystemConfig.audit.delete_node.name:
      return (
        <span>
          {t(Strings.audit_space_node_delete_detail_start, {
            nodeType: ConfigConstant.nodeNameMap.get(record.body.node.nodeType),
          })}
          <a href={`/workbench/${record.body.node.nodeId}`} className="auditSourceLink" target="_self">
            {record.body.node.currentNodeName}
          </a>
        </span>
      );
    case SystemConfig.audit.store_share_node.name:
      return (
        <span>
          {t(Strings.audit_store_share_node_detail_start, {
            nodeType: ConfigConstant.nodeNameMap.get(record.body.node.nodeType),
          })}
          <a href={`/workbench/${record.body.node.nodeId}`} className="auditSourceLink" target="_self">
            {record.body.node.currentNodeName}
          </a>
        </span>
      );
    case SystemConfig.audit.enable_node_role.name:
      return (
        <span>
          {t(Strings.audit_enable_node_role_detail_start, {
            nodeType: ConfigConstant.nodeNameMap.get(record.body.node.nodeType),
          })}
          <a href={`/workbench/${record.body.node.nodeId}`} className="auditSourceLink" target="_self">
            {record.body.node.currentNodeName}
          </a>
          {t(Strings.audit_enable_node_role_detail_end)}
        </span>
      );
    case SystemConfig.audit.disable_node_role.name:
      return (
        <span>
          {t(Strings.audit_disable_node_role_detail_start, {
            nodeType: ConfigConstant.nodeNameMap.get(record.body.node.nodeType),
          })}
          <a href={`/workbench/${record.body.node.nodeId}`} className="auditSourceLink" target="_self">
            {record.body.node.currentNodeName}
          </a>
          {t(Strings.audit_disable_node_role_detail_end)}
        </span>
      );
    case SystemConfig.audit.add_node_role.name:
      return (
        <span>
          {t(Strings.audit_add_node_role_detail_start, {
            unitNames: record.body.units?.map((unit: any) => unit.name)?.join(','),
          })}
          <a href={`/workbench/${record.body.node.nodeId}`} className="auditSourceLink" target="_self">
            {record.body.node.currentNodeName}
          </a>
          {t(Strings.audit_add_node_role_detail_end, {
            role: ConfigConstant.permissionText[record.body.control.role],
          })}
        </span>
      );
    case SystemConfig.audit.delete_node_role.name:
      return (
        <span>
          {t(Strings.audit_delete_node_role_detail_start, {
            unitNames: record.body.units?.map((unit: any) => unit.name)?.join(','),
          })}
          <a href={`/workbench/${record.body.node.nodeId}`} className="auditSourceLink" target="_self">
            {record.body.node.currentNodeName}
          </a>
          {t(Strings.audit_delete_node_role_detail_end, {
            role: ConfigConstant.permissionText[record.body.control.oldRole],
          })}
        </span>
      );
    case SystemConfig.audit.update_node_role.name:
      return (
        <span>
          {t(Strings.audit_update_node_role_detail_start, {
            unitNames: record.body.units?.map((unit: any) => unit.name)?.join(','),
          })}
          <a href={`/workbench/${record.body.node.nodeId}`} className="auditSourceLink" target="_self">
            {record.body.node.currentNodeName}
          </a>
          {t(Strings.audit_update_node_role_detail_end, {
            role: ConfigConstant.permissionText[record.body.control.role],
          })}
        </span>
      );
    default:
      return <></>;
  }
};

const langCode = getLanguage();

const isMember = (object: any): object is IMember => 'memberId' in object;

export const Log = () => {
  const spaceId = useSelector((state: IReduxState) => state.space.activeId);
  const spaceInfo = useSelector((state: IReduxState) => state.space.curSpaceInfo);
  const subscription = useSelector((state: IReduxState) => state.billing.subscription, shallowEqual);

  const [state, setState] = useState<ILogSearchState>({
    dates: null,
    actions: [],
    keyword: '',
  });
  const [pagination, setPagination] = useState<ILogTablePagination>({
    pageSize: 10,
    pageNum: 1,
  });
  const [total, setTotal] = useState<number>(0);
  const [tableData, setTableData] = useState<any[]>([]);
  const [selectedMembers, setSelectedMembers] = useState<IMember[]>([]);
  const [showTrialModal, setShowTrialModal] = useState<boolean>(!subscription?.maxAuditQueryDays);

  const { run: getSpaceAudit, loading } = useRequest(async () => {
    if (!spaceId) return;

    const res = await Api.getSpaceAudit({
      spaceId,
      actions: state.actions,
      keyword: state.keyword,
      memberIds: selectedMembers.map((member: IMember) => member.memberId),
      beginTime: state.dates?.[0] ? dayjs(state.dates[0]).startOf('day').format('YYYY-MM-DD HH:mm:ss') : undefined,
      endTime: state.dates?.[1] ? dayjs(state.dates[1]).endOf('day').format('YYYY-MM-DD HH:mm:ss') : undefined,
      pageNo: pagination.pageNum,
      pageSize: pagination.pageSize,
    });

    if (res?.data?.success) {
      setTotal(res.data?.data?.total ?? 0);
      setTableData(res.data?.data?.records?.map((record: any, index: number) => ({
        id: index,
        action: record?.action,
        createdAt: record?.createdAt ? dayjs(record.createdAt).format('YYYY-MM-DD HH:mm:ss') : '',
        operator: record?.operator || {},
        body: record?.body || {},
      })) || []);
    }
  }, { manual: true });
  
  const auditTypeOptions = useMemo(() => auditTypeMapToList(SystemConfig.audit as any), []);
  const isWecomSpace = isSocialWecom(spaceInfo);

  const onOpenMemberModal = () => {
    expandUnitModal({
      checkedList: selectedMembers,
      isSingleSelect: false,
      hiddenInviteBtn: true,
      onSubmit: onMemberSelectDone,
      source: SelectUnitSource.Admin,
      allowEmtpyCheckedList: true,
    });
  };

  const onMemberSelectDone = (values: UnitItem[]) => {
    const members: IMember[] = values.filter(value => isMember(value)) as IMember[];
    setSelectedMembers(members);
  };

  const onPaginationChange = (newPageNum: number, newPageSize: number | undefined) => {
    setPagination({
      pageNum: newPageNum,
      pageSize: newPageSize ?? 0,
    });
  };

  const onPreventMouseDown = (event: React.MouseEvent<HTMLSpanElement>) => {
    event.preventDefault();
    event.stopPropagation();
  };

  const onReset = () => {
    setState({
      dates: null,
      actions: [],
      keyword: '',
    });
    setSelectedMembers([]);
  };

  const resetPaginationAndSearch = () => {
    if (pagination.pageNum === 1) {
      onSearch();
      return;
    }

    setPagination({
      pageSize: 10,
      pageNum: 1,
    });
  };

  const onSearch = () => {
    if (loading) return;

    getSpaceAudit();
  };

  const renderAction = (_text: any, record: any) => <ActionToTemplate record={record} />;

  const renderMemberNames = () => {
    if (isWecomSpace) {
      return selectedMembers.map((member: IMember) => getSocialWecomUnitName({
        name: member.originName || member.memberName,
        isModified: member?.isMemberNameModified,
        spaceInfo,
      }));
    }

    return selectedMembers.map(member => member.originName || member.memberName);
  };

  useEffect(() => {
    onSearch();
  }, [pagination.pageNum, pagination.pageSize]); // eslint-disable-line

  useEffect(() => {
    resetPaginationAndSearch();
  }, [selectedMembers, state.dates]); // eslint-disable-line

  if (showTrialModal) {
    return <Trial setShowTrialModal={setShowTrialModal} title={t(Strings.space_log_title)}/>;
  }

  return (
    <div className={styles.logContainer}>
      <div className={styles.titleRow}>
        <h1 className={styles.title}>
          {t(Strings.space_log_title)}
        </h1>
        <span className={styles.spaceLevel}>{t(Strings.enterprise)}</span>
      </div>
      <h2 className={styles.desc}>
        {t(Strings.space_log_trial_desc1, { days: subscription?.maxAuditQueryDays })}
      </h2>
      <div className={styles.logSearchPanelContainer}>
        <div className={styles.item}>
          <span className={styles.label}>{t(Strings.space_log_date_range)}</span>
          <DatePicker.RangePicker
            className={classnames([styles.itemInput, styles.rangePicker])}
            disabledDate={current => current && current > dayjs().endOf('day') || current < dayjs()
              .subtract(subscription?.maxAuditQueryDays || 0, 'day')
              .startOf('day')
            }
            locale={LocalFormat.getLocal(langCode)}
            onCalendarChange={(dates: [Dayjs | null, Dayjs | null] | null) => setState({ ...state, dates })}
            placeholder={[t(Strings.start_time), t(Strings.end_time)]}
            suffixIcon={null}
            value={state.dates}
          />
        </div>
        <div className={classnames([styles.item, styles.operatorItem])}>
          <span className={styles.label}>{t(Strings.space_log_operator)}</span>
          <div className={styles.operatorRenderer}>
            {renderMemberNames().map((member, i, members) => (
              <span key={i} className={styles.operator}>
                {member}
                {i < members.length - 1 ? ',' : ''}
              </span>
            ))}
          </div>
          <AntSelect
            className={classnames([styles.itemSelect, styles.operatorSelect])}
            dropdownMatchSelectWidth
            onClick={onOpenMemberModal}
            open={false}
            showArrow
            showSearch={false}
            size='middle'
            value=''
            virtual={false}
          />
        </div>
        <div className={styles.item}>
          <span className={styles.label}>{t(Strings.space_log_action_type)}</span>
          <AntSelect
            className={classnames([styles.itemSelect])}
            dropdownClassName={styles.selectDropdown}
            maxTagPlaceholder={(value) => `... +${value.length} 条选项`}
            mode='multiple'
            onBlur={resetPaginationAndSearch}
            onChange={(value) => setState({ ...state, actions: value })}
            options={auditTypeOptions}
            showArrow
            showSearch={false}
            size='middle'
            tagRender={({ label, closable, onClose }) => (
              <Tag
                onMouseDown={onPreventMouseDown}
                closable={closable}
                onClose={onClose}
                className={styles.selectTag}
              >
                {label}
              </Tag>
            )}
            value={state.actions}
            virtual={false}
          />
        </div>
        <div className={styles.item}>
          <span className={styles.label}>{t(Strings.space_log_file_name)}</span>
          <Input
            value={state.keyword}
            onChange={(e) => setState({ ...state, keyword: e.target.value })}
            onPressEnter={resetPaginationAndSearch}
            className={classnames([styles.itemInput])}
          />
        </div>
        <div className={styles.buttons}>
          <IconButton
            className={styles.reloadButton}
            icon={ReloadOutlined}
            onClick={onReset}
            shape='square'
            variant='background'
          />
          <Button
            color='primary'
            disabled={loading}
            onClick={resetPaginationAndSearch}
            prefixIcon={<SearchOutlined />}
            size='middle'
          >
            {t(Strings.search)}
          </Button>
        </div>
      </div>
      <div className={styles.labelRow}>
        <h6 className={styles.subTitle}>{t(Strings.space_logs)}</h6>
      </div>
      <Table
        className={styles.logTable}
        columns={[{
          title: t(Strings.space_log_action_time),
          dataIndex: 'createdAt',
          key: 'createdAt',
          align: 'left',
          width: 260,
        },
        {
          title: t(Strings.space_log_operator),
          dataIndex: 'operator',
          key: 'operator',
          align: 'left',
          width: 260,
          render: (value) => <MemberItem
            unitInfo={{
              type: MemberType.Member,
              userId: value?.memberId,
              name: value?.memberName,
              avatar: value?.avatar,
              isActive: value?.isActive,
            }}
          />,
        },
        {
          title: t(Strings.space_log_actions),
          dataIndex: 'action',
          key: 'action',
          align: 'left',
          render: renderAction,
        }]}
        dataSource={tableData}
        loading={loading}
        pagination={{
          position: ['bottomRight'],
          showSizeChanger: false,
          onChange: (newPageNum, newPageSize) => onPaginationChange(newPageNum, newPageSize),
          pageSize: pagination.pageSize,
          current: pagination.pageNum,
          total,
        }}
        rowKey={(record: any) => `${record.createdAt}-${record.operator.memberId}-${record.id}`}
      />
    </div>
  );
};
