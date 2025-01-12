import { Injectable } from '@nestjs/common';
import dayjs from 'dayjs';
import { MoreThan } from 'typeorm';
import {
  databus,
  FieldType,
  generateRandomString, ISnapshot,
  // @ts-ignore
  ITableBundleInitOptions,
  ITextSegment,
  // @ts-ignore
  TableBundle,
  // @ts-ignore
  TableBundleDataSheet,
  // @ts-ignore
  TableBundleSnapshot
} from '@apitable/core';
import { StatusCode, TableBundleType } from '../dto/interface';
import { CommandService } from 'database/command/services/command.service';
import path from 'path';
import { statSync } from 'fs';
import { TimeMachineDataStorageProvider } from './time.machine.data.storage.provider';
import { readFile } from 'fs/promises';
import * as os from 'os';
import { Logger } from 'winston';
import fs from 'fs';
import { EnvConfigKey, InjectLogger } from 'shared/common';
import { EnvConfigService } from 'shared/services/config/env.config.service';
import { RestService } from 'shared/services/rest/rest.service';
import { NodeService } from 'node/services/node.service';
import { NodeDescriptionService } from 'node/services/node.description.service';
import { DatasheetService } from 'database/datasheet/services/datasheet.service';
import { DatasheetMetaService } from 'database/datasheet/services/datasheet.meta.service';
import { DatasheetRecordService } from 'database/datasheet/services/datasheet.record.service';
import { UserService } from 'user/services/user.service';
import { TablebundleBaseDto } from '../dto/tablebundle.base.dto';
import { UserBaseInfoDto } from '../../../../user/dtos/user.dto';
import { DatasheetException, PermissionException, ServerException } from '../../../../shared/exception';
import { IOssConfig } from '../../../../shared/interfaces';
import { IdWorker } from '../../../../shared/helpers';
import { AttachmentTypeEnum } from '../../../../shared/enums/attachment.enum';
import { TableBundleRepository } from '../repositories/tablebundle.repository';
import { TableBundleEntity } from 'database/time_machine/entities/tablebundle.entity';
import { TableBundleLoader, TableBundleSaver } from 'database/time_machine/entities/table.bundle.options';
import { TimeMachineBaseService } from 'database/time_machine/time.machine.service.base';
import { AutomationService } from 'automation/services/automation.service';
import { RecordCommentService } from 'database/datasheet/services/record.comment.service';
import {DatasheetChangesetService} from 'database/datasheet/services/datasheet.changeset.service';
import process from "process";

@Injectable()
export class TimeMachineService extends TimeMachineBaseService{
  private readonly databus: databus.DataBus;
  private readonly database: databus.Database;
  // @ts-ignore
  constructor(
      @InjectLogger() private readonly logger: Logger,
      private readonly envConfigService: EnvConfigService,
      private readonly tableBundleRepository: TableBundleRepository,
      private readonly restService: RestService,
      private readonly nodeService: NodeService,
      readonly datasheetService: DatasheetService,
      private readonly datasheetMetaService: DatasheetMetaService,
      private readonly datasheetRecordService: DatasheetRecordService,
      private readonly userService: UserService,
      private readonly automationService: AutomationService,
      private readonly recordCommentService: RecordCommentService,
      private readonly datasheetChangesetService: DatasheetChangesetService,
      private readonly nodeDescriptionService: NodeDescriptionService,
      readonly commandService: CommandService,
  ) {
    super();
    this.databus = databus.DataBus.create({
      dataStorageProvider: new TimeMachineDataStorageProvider(datasheetService),
      storeProvider: {
        createDatasheetStore: datasheetPack => Promise.resolve(commandService.fullFillStore(datasheetPack)),
        createDashboardStore: dashboardPack => {
          throw new Error('unreachable ' + dashboardPack.dashboard.id);
        },
      },
    });

    this.database = this.databus.getDatabase();
  }

  public override async getTableBundleById(nodeId: string, id?: string): Promise<any[]> {
    let bundleList = null;
    if (!id) {
      bundleList = await this.tableBundleRepository.find({
        where: {
          dstId: nodeId
        },
        order: {
          createdAt: 'DESC'
        }
      });
    }else {
      bundleList = await this.tableBundleRepository.find({
        where: {
          dstId: nodeId,
          tbdId: id
        },
        order: {
          createdAt: 'DESC'
        }
      });
    }
    const idSet = new Set<string>();
    bundleList.forEach(bundle => {
      if (bundle.deletedBy){
        idSet.add(bundle.deletedBy);
      }
      idSet.add(bundle.createdBy)
    });
    const userMap = new Map<string, UserBaseInfoDto>();
    if (idSet.size > 0) {
      const users = await this.userService.selectUserBaseInfoByIds(Array.from(idSet) as any[]);
      users.forEach(user => {
        userMap.set(user.id, user);
      });
    }
    const bundleResult: TablebundleBaseDto[] = [];
    bundleList.forEach(bundle => {
      const user = userMap.get(bundle.createdBy);
      let deleteUser: any = {};
      if (bundle.deletedBy) {
        deleteUser = userMap.get(bundle.deletedBy);
      }
      const tmp: TablebundleBaseDto = {
        ...bundle,
        creatorInfo: user,
        deleteInfo: deleteUser
      };
      bundleResult.push(tmp);
    });
    return bundleResult;
  }

  public override async renameTableBundle(tablebundleId: string, name: string) {
    return await this.tableBundleRepository.update({ tbdId: tablebundleId }, { name });
  }

  public override async deleteTableBundle(nodeId: string,tablebundleId: string, userId: string) {
    return await this.tableBundleRepository.update({ tbdId: tablebundleId },
        { dstId: nodeId, isDeleted: true, deletedBy: userId, deletedAt: new Date() });
  }

  async checkSnapshot(cookie: string, spaceId: string, dstId: string) {
    const subcriInfo = await this.restService.getSpaceSubscriptionInfo(cookie, spaceId);
    const snapCount = await this.tableBundleRepository.count({
      where: {
        isDeleted: false,
        dstId: dstId,
        expiredAt: MoreThan(new Date().getTime()),
      },
    });
    const product = subcriInfo.product ? subcriInfo.product.toLowerCase() : '';
    const productMap = {
      free: 1,
      ce: 1,
      dingtalk_base: 1,
      wecom_base: 1,
      feishu_base: 1,
      plus: 2,
      dingtalk_standard: 2,
      wecom_standard: 2,
      feishu_standard: 2,
      pro: 3,
      dingtalk_profession: 3,
      wecom_profession: 3,
      feishu_profession: 3,
      bronze: 1,
      silver: 2,
      gold: 3,
      feishu_enterprise: 4,
      wecom_enterprise: 4,
      private_cloud: 4,
      dingtalk_enterprise: 4,
      appsumo_tier1: 4,
      appsumo_tier2: 4,
      appsumo_tier3: 4,
      appsumo_tier4: 4,
      appsumo_tier5: 4,
      'exclusive limited tier 1': 4,
      'exclusive limited tier 2': 4,
      'exclusive limited tier 3': 4,
      'exclusive limited tier 4': 4,
      'exclusive limited tier 5': 4,
      enterprise: 4,
      business: 4,
    };
    if (!productMap[product]) {
      throw new ServerException(PermissionException.OPERATION_DENIED);
    }
    if (snapCount >= productMap[product]) {
      return {
        success: false,
        code: 400,
        message: 'api_params_tablebundle_max_count_error',
        data: {
          count: productMap[product],
          product: subcriInfo.product
        }
      };
    }
    return null;
  }

  public override async getDataPack(_cookie: string, dstId: string, _spaceId: string, _userId: string): Promise<any> {
    const dataSheet = await this.database.getDatasheet(dstId, {} as databus.IDatasheetOptions);
    const r: any = {};
    if (dataSheet != null) {
      const snapshot = JSON.parse(JSON.stringify(dataSheet.snapshot));
      r.o = JSON.parse(JSON.stringify(dataSheet.snapshot));
      const fieldSet = new Set<string>();
      for (const fieldMapKey in snapshot.meta.fieldMap) {
        const fieldType = snapshot.meta.fieldMap[fieldMapKey]?.type;
        if (fieldType && (fieldType === FieldType.LookUp || fieldType === FieldType.OneWayLink || fieldType === FieldType.Link || fieldType === FieldType.Cascader)) {
          if (snapshot.meta.fieldMap[fieldMapKey]?.property && snapshot.meta.fieldMap[fieldMapKey]?.property?.foreignDatasheetId == dstId) {
            continue;
          }
          fieldSet.add(fieldMapKey);
        }
      }
      if (fieldSet.size > 0) {
        for (const recordMapKey in snapshot.recordMap) {
          const record = snapshot.recordMap[recordMapKey];
          if (record != undefined) {
            fieldSet.forEach((fieldId: string) => {
              const newVal = dataSheet.cellValue(fieldId, recordMapKey);
              if (snapshot && snapshot.recordMap && snapshot.recordMap[recordMapKey]) {
                const newCell = { text: newVal, type: 1 } as ITextSegment;
                record.data[fieldId] = [newCell];
              }
            });
          }
        }
        fieldSet.forEach((fieldId: string) => {
          const fieldMeta = snapshot.meta.fieldMap[fieldId];
          if (fieldMeta) {
            fieldMeta.type = FieldType.SingleText;
            fieldMeta.property = { defaultValue: '' };
          }
        });
      }
      r.n = snapshot;
    }
    r.extras = await this.getExtraInfo(dstId);
    let extraStr = JSON.stringify(r.extras);
    const regex = new RegExp(dstId, 'g');
    extraStr = extraStr.replace(regex, 'dstNewxxxxx');
    r.newExtras = JSON.parse(extraStr);
    return r;
  }

  async getExtraInfo(dstId: string) {
    const extras: any = {};
    const robots = await this.automationService.getRobotsByDstId(dstId);
    extras.robots = robots;
    if (robots){
      const robotIds = new Set<string>();
      robots.map(robot => {
        return robot.robotId;
      }).forEach(id => robotIds.add(id));
      extras.actions = await this.automationService.getActionByRobotIds(Array.from(robotIds));
      extras.triggers = await this.automationService.getTriggersByRobotIds(Array.from(robotIds));
    }
    extras.comments = await this.recordCommentService.getAllCommentsByDstId(dstId);
    if (extras.comments) {
      const changesets = await this.datasheetChangesetService.getAllCommentChangeSetByDstId(dstId);
      if (changesets && changesets[0]) {
        const newChangeSet = changesets[0];
        const ops: any[] = [];
        changesets.forEach(changeset => {
          changeset.operations?.forEach(op => {
            if (op.cmd == 'InsertComment') {
              ops.push(op);
            }
          });
        });
        newChangeSet.operations = ops;
        extras.changeSets = [newChangeSet];
      }
    }
    const icon = await this.nodeService.getNodeIcon(dstId);
    if (icon) {
      extras.icons = {id: dstId, icon};
    }
    extras.desc = [await this.nodeDescriptionService.getNodeDesc(dstId)];
    return extras;
  }

  public override async generateTableBundle(cookie: string, dstId: string, spaceId: string, userId: string) {
    const checkRsult = await this.checkSnapshot(cookie, spaceId, dstId);
    if (checkRsult) {
      return checkRsult;
    }
    const tb = new TableBundleEntity();
    tb.dstId = dstId;
    tb.spaceId = spaceId;
    tb.createdBy = userId;
    tb.createdAt = new Date();
    tb.expiredAt = String(new Date().getTime() + 1000 * 60 * 60 * 24 * 30 * 2);
    tb.tablebundleUrl = '';
    tb.tbdId = 'tbd' + generateRandomString(15);
    tb.type = TableBundleType.Snapshot;
    tb.statusCode = StatusCode.Initiation;
    tb.name = dayjs(tb.createdAt.getTime()).format('YYYY-MM-DD HH:mm');
    const result = await this.tableBundleRepository.save(tb);
    this.createAndUploadTableBundle(cookie, dstId, tb.tbdId).then(() => {
      this.logger.info(`generate tablebundle for ${dstId} tabId is ${tb.tbdId}`);
    }).catch(e => {
      this.logger.error('create tablebundle error ', e);
    });
    return result;
  }

  public override async recoverTableBundle(
      userId: string,
      tablebundleId: string,
      spaceId :string,
      dstId: string,
      folderId: string,
      name: string
  ): Promise<any> {
    const tablebundle = await this.tableBundleRepository.findOne({ tbdId: tablebundleId, dstId });
    if (!tablebundle || tablebundle.isDeleted) {
      throw new ServerException(DatasheetException.NOT_EXIST);
    }
    if (!name){
      name = 'recover';
    }
    const nodeName = tablebundle.name + ' ' + name;
    const preNodeId = await this.nodeService.getFolderLastChildren(folderId);
    const fileName = `${dstId}.tablebundle`;
    const filePath = await this.restService.downloadFile(this.getTimeOssHost(), tablebundle.tablebundleUrl, fileName);
    const tb = TableBundle.new(this.tableBundleOption());
    tb.loadFile(filePath);
    const dataSheet = tb.getDataSheet(dstId);
    fs.unlinkSync(filePath);
    if (!dataSheet) {
      throw new ServerException(DatasheetException.NOT_EXIST);
    }
    const dsts :any[] = [], newNodes: any[] = [], newDataSheetMeta = [], newRecords = [];
    const newDstId = 'dst' + generateRandomString(15);
    newNodes.push({
      id: IdWorker.nextId().toString(),
      spaceId,
      parentId: folderId,
      preNodeId,
      nodeId: newDstId,
      nodeName,
      type: 2,
      isTemplate: false,
      createdBy: userId,
      updatedBy: userId
    });
    dsts.push({
      id: IdWorker.nextId().toString(),
      spaceId,
      dstId: newDstId,
      nodeId: newDstId,
      dstName: nodeName,
      revision: 0,
      createdBy: userId,
      updatedBy: userId
    });
    const snp = dataSheet.snapshot as ISnapshot;
    const meta = snp.meta;
    // handle self lookup field
    for (const fieldMapKey in meta.fieldMap) {
      const fieldType = meta.fieldMap[fieldMapKey]?.type;
      if (fieldType && (fieldType === FieldType.LookUp || fieldType === FieldType.OneWayLink || fieldType === FieldType.Link || fieldType === FieldType.Cascader)) {
        const property = meta.fieldMap[fieldMapKey]?.property;
        if (property && property.foreignDatasheetId) {
          property.foreignDatasheetId = newDstId;
        }
      }
    }
    newDataSheetMeta.push({
      id: IdWorker.nextId().toString(),
      dstId: newDstId,
      metaData: meta,
      createdBy: userId,
      updatedBy: userId,
      revision: 0
    });
    const recordMap = snp.recordMap;
    for (const recordMapKey in recordMap) {
      const record = recordMap[recordMapKey];
      if (record) {
        newRecords.push({
          id: IdWorker.nextId().toString(),
          dstId: newDstId,
          recordId: recordMapKey,
          data: record.data,
          revision: 0,
          revisionHistory: '0',
          recordMeta: record.recordMeta,
          createdBy: userId,
          updatedBy: userId
        });
      }
    }
    let extraStr = dataSheet.extras;
    if (extraStr){
      const regex = new RegExp(dstId, 'g');
      extraStr = extraStr.replace(regex, newDstId);
      const extra = JSON.parse(extraStr);
      this.automationService.recoverRobots(extra.robots, extra.actions, extra.triggers);
      this.recordCommentService.recoverComments(extra.comments);
      this.datasheetChangesetService.recoverChangeSets(newDstId, extra.changeSets);
      this.nodeDescriptionService.recoverNodeDesc(extra.desc);
      if (extra.icons){
        newNodes.forEach(node => {
            node.icon = extra.icons[newDstId];
        })
      }
    }

    await this.nodeService.batchSave(newNodes);
    await this.datasheetMetaService.batchSave(newDataSheetMeta);
    await this.datasheetRecordService.batchSave(newRecords);
    await this.datasheetService.batchSave(dsts);
    return { dstId :newDstId, nodeName, spaceId, parentId: folderId, preNodeId };
  }

  public override async previewTableBundle(tablebundleId: string, nodeId: string): Promise<any> {
    const tablebundle = await this.tableBundleRepository.findOne({ tbdId: tablebundleId, dstId: nodeId });
    if (!tablebundle) {
      throw new ServerException(DatasheetException.NOT_EXIST);
    }
    const fileName = `${nodeId}.tablebundle`;
    const filePath = await this.restService.downloadFile(this.getTimeOssHost(), tablebundle.tablebundleUrl, fileName);
    const tb = TableBundle.new(this.tableBundleOption());
    tb.loadFile(filePath);
    const dataSheet = tb.getDataSheet(nodeId);
    fs.unlinkSync(filePath);
    return dataSheet;
  }

  public override async downloadTableBundle(tablebundleId: string, nodeId: string, fileName: string): Promise<string> {
    const tablebundle = await this.tableBundleRepository.findOne({ tbdId: tablebundleId, dstId: nodeId });
    if (!tablebundle) {
      throw new ServerException(DatasheetException.NOT_EXIST);
    }
    const filePath = await this.restService.downloadFile(this.getTimeOssHost(), tablebundle.tablebundleUrl, fileName);
    return new Promise((resolve) => {
      resolve(filePath);
    });
  }

  private getTimeOssHost(): string {
    const oss = this.envConfigService.getRoomConfig(EnvConfigKey.OSS) as IOssConfig;
    let ossHost = oss.host
    if (!ossHost.startsWith('http')) {
      let env = process.env.OSS_CLIENT_TYPE || 'aws';
      if (env == 'aws') {
        ossHost = process.env.AWS_ENDPOINT || '';
      }else if (env == 'qiniu') {
        ossHost = process.env.QINIU_DOWNLOAD_DOMAIN || '';
      }else if (env == 'huawei-cloud') {
        ossHost = process.env.HUAWEICLOUD_OBS_ENDPOINT || '';
      }else if (env == 'minio') {
        ossHost = process.env.MINIO_ENDPOINT || '';
      }
      if (ossHost) {
          if (ossHost.endsWith('/')) {
              ossHost = ossHost + (process.env.ASSETS_BUCKET || '');
          }else {
              ossHost = ossHost + '/' + (process.env.ASSETS_BUCKET || '');
          }
      }
    }
    if (!ossHost){
      console.warn('failed to get oss host by process env. reset to be ' + oss.host);
      ossHost = oss.host;
    }
    return ossHost;
  }

  async createAndUploadTableBundle(cookie: string, dstId: string, tbdId: string) {
    const dataSheet = await this.database.getDatasheet(dstId, {} as databus.IDatasheetOptions);
    if (dataSheet != null) {
      const snapshot = JSON.parse(JSON.stringify(dataSheet.snapshot));
      const fieldSet = new Set<string>();
      for (const fieldMapKey in snapshot.meta.fieldMap) {
        const fieldType = snapshot.meta.fieldMap[fieldMapKey]?.type;
        if (fieldType && (fieldType === FieldType.LookUp || fieldType === FieldType.OneWayLink || fieldType === FieldType.Link || fieldType === FieldType.Cascader)) {
          if (snapshot.meta.fieldMap[fieldMapKey]?.property?.foreignDatasheetId == dstId){
            continue;
          }
          fieldSet.add(fieldMapKey);
        }
      }
      if (fieldSet.size > 0) {
        for (const recordMapKey in snapshot.recordMap) {
          const record = snapshot.recordMap[recordMapKey];
          if (record != undefined) {
            fieldSet.forEach((fieldId: string) => {
              const newVal = dataSheet.cellValue(fieldId, recordMapKey);
              if (snapshot && snapshot.recordMap && snapshot.recordMap[recordMapKey]) {
                const newCell = { text: newVal, type: 1 } as ITextSegment;
                record.data[fieldId] = [newCell];
              }
            });
          }
        }
        fieldSet.forEach((fieldId: string) => {
          const fieldMeta = snapshot.meta.fieldMap[fieldId];
          if (fieldMeta) {
            fieldMeta.type = FieldType.SingleText;
            fieldMeta.property = { defaultValue: '' };
          }
        });
      }

      const tableBundleFile = path.join(os.tmpdir(), `${dstId}.tablebundle`);
      const tableBundle = TableBundle.new(this.tableBundleOption());
      const extras = await this.getExtraInfo(dstId);
      tableBundle.apply(new TableBundleDataSheet(this.transformSnapshot(snapshot), JSON.stringify(extras)), dstId);
      tableBundle.save(tableBundleFile);
      const res = await this.restService.getUploadPresignedUrl({ cookie }, dstId, 1, AttachmentTypeEnum.DATASHEET_ATTACH);
      const assetInfo = res[0];
      if (assetInfo) {
        const fileSize = statSync(tableBundleFile).size;
        const fileData = await readFile(tableBundleFile);
        await this.restService.uploadFile(fileData, assetInfo.uploadUrl, assetInfo.uploadRequestMethod, fileSize);
        await this.restService.getUploadCallBack({ cookie }, [assetInfo.token], AttachmentTypeEnum.DATASHEET_ATTACH);
        await this.tableBundleRepository.update({ tbdId }, {
          statusCode: StatusCode.Completion,
          tablebundleUrl: assetInfo.token
        });

        fs.unlinkSync(tableBundleFile);
      }
    }
  }

  private transformSnapshot(snapshot: any):TableBundleSnapshot {
    return snapshot as TableBundleSnapshot;
  }

  private tableBundleOption(): ITableBundleInitOptions {
    const options: ITableBundleInitOptions = {
      loader: new TableBundleLoader(),
      saver: new TableBundleSaver(),
    };
    return options;
  }
}