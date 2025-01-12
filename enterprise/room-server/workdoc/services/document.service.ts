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

import { Injectable } from '@nestjs/common';
import { TiptapTransformer } from '@hocuspocus/transformer';
import * as Y from 'yjs';
import { DocumentDaoApiService } from 'enterprise/share/databus_client/api/document.dao.api.service';
import {
  DocumentAssetStatisticRo,
  DocumentAssetStatisticResult_DocumentAssetStatisticData,
  DocumentAssetStatisticResult_DocumentAssetStatisticInfo,
} from 'grpc/generated/serving/RoomServingService';
import { InjectLogger } from 'shared/common';
import { HttpHelper } from 'shared/helpers';
import { Logger } from 'winston';
import { DocumentBaseService } from 'workdoc/services/document.base.service';
import { DocumentTypeEnum } from '../enums';
import { IDocumentEventContext, IWorkDocFieldAuthParameters } from '../interfaces';

@Injectable()
export class DocumentService extends DocumentBaseService {
  constructor(
    // @ts-ignore
    @InjectLogger() private readonly logger: Logger,
    private readonly daoApiService: DocumentDaoApiService
  ) {
    super();
  }

  override async updateRecordIdProps(resourceId: string, documentNames: string[], recordId: string) {
    const ro = {
      resource_id: resourceId,
      document_names: documentNames,
      record_id: recordId,
    };
    await HttpHelper.getResponseData(this.daoApiService.daoUpdateDocumentProps(ro));
  }

  override async documentAssetStatistic(ro: DocumentAssetStatisticRo): Promise<DocumentAssetStatisticResult_DocumentAssetStatisticData> {
    const infos: DocumentAssetStatisticResult_DocumentAssetStatisticInfo[] = [];
    for (const info of ro.infos) {
      const data = await this.fetchData(info.documentName);
      if (!data) {
        const assetInfos = info.fileUrls.map((fileUrl) => {
          return { fileUrl, cite: 0 };
        });
        infos.push({ documentName: info.documentName, assetInfos });
        continue;
      }
      const docs = new Y.Doc();
      if (data) {
        Y.applyUpdate(docs, data);
      }
      const str = JSON.stringify(TiptapTransformer.fromYdoc(docs));
      const assetInfos = info.fileUrls.map((fileUrl) => {
        const regex = new RegExp(fileUrl, 'g');
        const matches = str.match(regex);
        const cite = matches ? matches.length : 0;
        return { fileUrl, cite };
      });
      infos.push({ documentName: info.documentName, assetInfos });
    }
    return { infos };
  }

  async generateNewDocumentName(): Promise<string> {
    return await HttpHelper.getResponseData(this.daoApiService.daoGetNewDocumentName());
  }

  async fetchData(documentName: string): Promise<any | undefined> {
    const data = await HttpHelper.getResponseData(this.daoApiService.daoGetDocumentData(documentName));
    return data && Buffer.from(data);
  }

  async storeData(documentName: string, data: Uint8Array, context: IDocumentEventContext) {
    const { spaceId, userId, resourceId, documentType, title, shareId, embedId } = context;
    let props;
    if (documentType == DocumentTypeEnum.WORKDOC_FIELD) {
      const { recordId, fieldId, formId, mirrorId } = context as Partial<IWorkDocFieldAuthParameters>;
      props = JSON.stringify({ recordId, fieldId, formId, shareId, mirrorId, embedId });
    }
    const ro = {
      space_id: spaceId,
      resource_id: resourceId,
      document_type: documentType,
      title,
      data: Array.from(data, (value) => value),
      props,
      updated_by: userId,
    };
    try {
      await HttpHelper.getResponseData(this.daoApiService.daoCreateOrUpdateDocument(documentName, ro));
    } catch (error) {
      this.logger.error(error);
    }
  }

  async createDocumentOperation(documentName: string, update: Uint8Array, context: IDocumentEventContext) {
    const { spaceId, userId } = context;
    const ro = {
      created_by: userId,
      space_id: spaceId,
      update_data: Array.from(update, (value) => value),
    };
    try {
      await HttpHelper.getResponseData(this.daoApiService.daoCreateDocumentOperation(documentName, ro));
    } catch (error) {
      this.logger.error(error);
    }
  }
}
