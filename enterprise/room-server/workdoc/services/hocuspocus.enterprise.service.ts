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

import { Database } from '@hocuspocus/extension-database';
import { Redis } from '@hocuspocus/extension-redis';
import { Hocuspocus, Server } from '@hocuspocus/server';
import { Injectable } from '@nestjs/common';
import { InjectLogger } from 'shared/common';
import { Logger } from 'winston';
import * as Y from 'yjs';
import { EmbedPermissionType } from 'enterprise/embed/models/embedlink.model';
import { IEmbedLinkBaseInfo } from 'enterprise/embed/repositories/embedlink.repository';
import { EmbedLinkService } from 'enterprise/embed/services/embedlink.service';
import { NodePermissionService } from 'node/services/node.permission.service';
import { NodeService } from 'node/services/node.service';
import { NodeShareSettingService } from 'node/services/node.share.setting.service';
import { ServerException, PermissionException } from 'shared/exception';
import { getIPAddress } from 'shared/helpers/system.helper';
import { NodePermission } from 'shared/interfaces';
import { redisModuleOptions } from 'shared/services/config';
import { UnitMemberService } from 'unit/services/unit.member.service';
import { UserService } from 'user/services/user.service';
import { HocuspocusBaseService } from 'workdoc/services/hocuspocus.base.service';
import { DocumentTypeEnum } from '../enums';
import { IAuthParameters, IDocumentEventContext, IWorkDocFieldAuthParameters } from '../interfaces';
import { DocumentService } from './document.service';

@Injectable()
export class HocuspocusEnterpriseService extends HocuspocusBaseService {
  constructor(
    // @ts-ignore
    @InjectLogger() private readonly logger: Logger,
    private readonly userService: UserService,
    private readonly memberService: UnitMemberService,
    private readonly nodeService: NodeService,
    private readonly nodePermissionService: NodePermissionService,
    private readonly nodeShareSettingService: NodeShareSettingService,
    private readonly documentService: DocumentService,
    private readonly embedLinkService: EmbedLinkService,
  ) {
    super();
  }

  override init(port: number): Hocuspocus {
    const redisOptions = redisModuleOptions();
    const documentService = this.documentService;

    const getAuthenticationNodeId = async (paramter: IAuthParameters): Promise<string> => {
      const { resourceId, documentType } = paramter;
      if (documentType == DocumentTypeEnum.WORKDOC_FIELD) {
        const { mirrorId, formId } = paramter as IWorkDocFieldAuthParameters;
        const nodeId = mirrorId || formId;
        if (nodeId) {
          const datasheetId = await this.nodeService.getMainNodeId(nodeId);
          if (resourceId !== datasheetId) {
            throw new ServerException(PermissionException.ACCESS_DENIED);
          }
          return nodeId;
        }
      }
      return resourceId;
    };

    const authenticate = async (requestParameters: URLSearchParams, cookie?: string): Promise<IDocumentEventContext & { hasPermission: boolean }> => {
      const paramter = parseAuthRequestParameters(requestParameters);
      const { resourceId, shareId, embedId } = paramter;
      let userId;
      let spaceId;
      let options;
      const nodeId = await getAuthenticationNodeId(paramter);
      if (shareId) {
        await this.nodeShareSettingService.checkNodeHasOpenShare(shareId, nodeId);
        userId = cookie && (await this.userService.getMeNullable(cookie)).userId;
        spaceId = await this.nodeService.getSpaceIdByNodeId(resourceId);
        options = { internal: false, shareId, form: (paramter as IWorkDocFieldAuthParameters).formId !== undefined };
      } else if (embedId) {
        // check if the node has been shared
        await this.embedLinkService.isEmbedLinkIdExist(embedId);
        const embedInfo: IEmbedLinkBaseInfo = await this.embedLinkService.getEmbedInfoByLinkId(embedId);
        const permissionType = embedInfo.props.payload!.permissionType;
        userId = cookie && (await this.userService.getMeNullable(cookie)).userId;
        spaceId = embedInfo.spaceId;
        switch (permissionType) {
          case EmbedPermissionType.ReadOnly:
            // non-datasheet
            if (embedInfo.nodeId !== resourceId) {
              throw new ServerException(PermissionException.ACCESS_DENIED);
            }
            return { userId: userId || '', spaceId, ...paramter, hasPermission: false };
          case EmbedPermissionType.PublicEdit:
            // non-datasheet and not logged in
            if (embedInfo.nodeId !== resourceId && !userId) {
              throw new ServerException(PermissionException.ACCESS_DENIED);
            }
            const permission = await this.nodePermissionService.getEmbedNodePermission(nodeId, { userId }, embedId);
            const hasPermission = getConnectionEditPermissionStatus(permission, paramter);
            return { userId: userId || '', spaceId, ...paramter, hasPermission };
          case EmbedPermissionType.PrivateEdit:
            if (!userId) {
              throw new ServerException(PermissionException.ACCESS_DENIED);
            }
            // check if the user is in this space
            await this.memberService.checkUserIfInSpace(userId, spaceId);
            options = { internal: true };
            break;
          default:
            options = { internal: true };
            break;
        }
      } else {
        userId = (await this.userService.getMe({ cookie })).userId;
        spaceId = await this.nodeService.checkUserForNode(userId, resourceId);
        options = { internal: true };
      }
      const permission = await this.nodePermissionService.getNodePermission(nodeId, { cookie }, options);
      if (!permission?.readable) {
        throw new ServerException(PermissionException.ACCESS_DENIED);
      }
      const hasPermission = getConnectionEditPermissionStatus(permission, paramter);
      return { userId: userId || '', spaceId, ...paramter, hasPermission };
    };

    const server = Server.configure({
      name: getIPAddress(),
      port,
      extensions: [
        new Redis({
          host: redisOptions.host,
          port: redisOptions.port,
          options: {
            password: redisOptions.password,
            db: redisOptions.db,
            tls: redisOptions.tls,
          },
        }),
        new Database({
          // Return a Promise to retrieve data …
          fetch: async ({ documentName, document, context, connection }) => {
            return new Promise(async (resolve, _reject) => {
              this.logger.debug(`Fetch ${documentName} document.`);
              const data = await this.documentService.fetchData(documentName);
              if (!data && !connection.readOnly) {
                await this.documentService.storeData(documentName, Buffer.from(Y.encodeStateAsUpdate(document)), context);
                this.logger.debug(`Auto create null document for ${documentName}.`);
              }
              resolve(data);
            });
          },
          // … and a Promise to store data:
          store: async ({ documentName, state, context }) => {
            this.logger.debug(`Store ${documentName} document.`);
            await this.documentService.storeData(documentName, state, context);
          },
        }),
      ],

      async onListen(data) {
        console.log(`Hocuspocus enterprise server[${data.configuration.name}] is listening on port "${data.port}"!`);
      },

      async onAuthenticate(data): Promise<IDocumentEventContext> {
        const context = await authenticate(data.requestParameters, data.requestHeaders.cookie);
        if (!context.hasPermission) {
          data.connection.readOnly = true;
        }
        // set contextual data to use it in other hooks
        return context;
      },

      async onChange({ documentName, update, context }) {
        await documentService.createDocumentOperation(documentName, update, context);
      },

      async onDisconnect({ document }) {
        if (document && document.getConnectionsCount() == 0) {
          server.unloadDocument(document);
        }
      },
    });
    return server;
  }
}

function parseAuthRequestParameters(requestParameters: URLSearchParams): IAuthParameters {
  const resourceId = requestParameters.get('resourceId');
  if (!resourceId) {
    throw new Error('Resource id not found');
  }
  const documentType = requestParameters.get('documentType');
  if (!documentType && !Number(documentType)) {
    throw new Error('Document type not found');
  }
  const paramter = {
    resourceId,
    documentType: Number(documentType),
    title: requestParameters.get('title') || undefined,
    shareId: requestParameters.get('shareId') || undefined,
    formId: requestParameters.get('formId') || undefined,
    mirrorId: requestParameters.get('mirrorId') || undefined,
    embedId: requestParameters.get('embedId') || undefined,
  };
  if (Number(documentType) == DocumentTypeEnum.WORKDOC_FIELD) {
    const recordId = requestParameters.get('recordId') || undefined;
    if (!recordId && !paramter.formId) {
      throw new Error('Record id not found');
    }
    const fieldId = requestParameters.get('fieldId');
    if (!fieldId) {
      throw new Error('Field id not found');
    }
    return { ...paramter, recordId, fieldId };
  }
  return paramter;
}

function getConnectionEditPermissionStatus(permission: NodePermission, paramter: IAuthParameters): boolean {
  if (paramter.documentType == DocumentTypeEnum.WORKDOC_FIELD) {
    if (!permission.fieldPermissionMap) {
      return permission.cellEditable;
    }
    const { fieldId, formId } = (paramter as IWorkDocFieldAuthParameters);
    const fieldPermission = permission.fieldPermissionMap[fieldId];
    if (!fieldPermission) {
      return permission.cellEditable;
    }
    if (formId) {
      return permission.cellEditable && fieldPermission.setting?.formSheetAccessible;
    }
    return fieldPermission.permission?.editable;
  }
  return permission.editable;
}
