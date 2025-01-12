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

import { Controller, Get } from "@nestjs/common";
import { DocumentService } from "../services/document.service";

@Controller('nest/v1')
export class DocumentController {
  constructor(
    private readonly documentService: DocumentService,
  ) {
  }

  @Get(['/documents/name'])
  async getDocumentName() {
    return await this.documentService.generateNewDocumentName();
  }

}
