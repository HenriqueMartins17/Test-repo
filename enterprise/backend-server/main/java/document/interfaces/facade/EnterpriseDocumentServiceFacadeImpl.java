/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up
 * license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its
 * subdirectories does not constitute permission to use this code or APITable Enterprise Edition
 * features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.document.interfaces.facade;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.apitable.enterprise.document.enums.DeleteWay;
import com.apitable.enterprise.document.mapper.DocumentMapper;
import com.apitable.enterprise.document.service.IDocumentService;
import com.apitable.interfaces.document.facade.DocumentServiceFacade;
import com.apitable.workspace.enums.IdRulePrefixEnum;
import java.util.List;
import java.util.stream.Collectors;

public class EnterpriseDocumentServiceFacadeImpl implements DocumentServiceFacade {

    private final IDocumentService iDocumentService;

    private final DocumentMapper documentMapper;

    public EnterpriseDocumentServiceFacadeImpl(IDocumentService iDocumentService, DocumentMapper documentMapper) {
        this.iDocumentService = iDocumentService;
        this.documentMapper = documentMapper;
    }

    @Override
    public String getSpaceIdByDocumentName(String documentName) {
        return iDocumentService.getSpaceIdByDocumentName(documentName, true);
    }

    @Override
    public void remove(Long userId, List<String> nodeIds) {
        List<String> datasheetIds = nodeIds.stream()
            .filter(i -> StrUtil.startWith(i, IdRulePrefixEnum.DST.getIdRulePrefixEnum()))
            .collect(Collectors.toList());
        if (datasheetIds.isEmpty()) {
            return;
        }
        List<String> documentNames = iDocumentService.getNamesByResourceIds(datasheetIds);
        if (documentNames.isEmpty()) {
            return;
        }
        iDocumentService.remove(userId, documentNames, DeleteWay.NODE);
    }

    @Override
    public void recover(Long userId, List<String> nodeIds) {
        List<String> datasheetIds = nodeIds.stream()
            .filter(i -> StrUtil.startWith(i, IdRulePrefixEnum.DST.getIdRulePrefixEnum()))
            .collect(Collectors.toList());
        if (datasheetIds.isEmpty()) {
            return;
        }
        List<String> documentNames = documentMapper.selectDeletedNamesByResourceIds(datasheetIds,
            DeleteWay.NODE.name().toLowerCase());
        if (documentNames.isEmpty()) {
            return;
        }
        iDocumentService.recover(userId, documentNames);
    }

    @Override
    public void cellValueOperate(Long userId, List<String> recoverDocumentNames,
            List<String> removeDocumentNames) {
        if (CollUtil.isNotEmpty(removeDocumentNames)) {
            iDocumentService.remove(userId, removeDocumentNames, DeleteWay.CELL);
            return;
        }
        if (CollUtil.isEmpty(recoverDocumentNames)) {
            return;
        }
        List<String> names = documentMapper.selectDeletedNamesByNames(recoverDocumentNames,
                DeleteWay.CELL.name().toLowerCase());
        if (names.isEmpty()) {
            return;
        }
        iDocumentService.recover(userId, names);
    }
}
