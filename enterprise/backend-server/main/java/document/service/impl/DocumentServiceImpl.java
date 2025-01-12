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

package com.apitable.enterprise.document.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.apitable.enterprise.document.enums.DeleteWay;
import com.apitable.enterprise.document.mapper.DocumentMapper;
import com.apitable.enterprise.document.model.DocumentDTO;
import com.apitable.enterprise.document.model.DocumentView;
import com.apitable.enterprise.document.service.IDocumentService;
import com.apitable.organization.entity.MemberEntity;
import com.apitable.organization.service.IMemberService;
import com.apitable.shared.util.IdUtil;
import com.apitable.space.service.ISpaceAssetService;
import com.apitable.user.entity.UserEntity;
import com.apitable.user.service.IUserService;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Document Service Implement Class.
 * </p>
 *
 * @author Chambers
 */
@Service
public class DocumentServiceImpl implements IDocumentService {

    @Resource
    private DocumentMapper documentMapper;

    @Resource
    private IUserService iUserService;

    @Resource
    private IMemberService iMemberService;

    @Resource
    private ISpaceAssetService iSpaceAssetService;

    @Override
    public String getSpaceIdByDocumentName(String documentName, boolean includeDeleted) {
        return documentMapper.selectSpaceIdByName(documentName, includeDeleted);
    }

    @Override
    public String getNewDocumentName() {
        while (true) {
            List<String> documentNames = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                String name = IdUtil.createDocumentName();
                documentNames.add(name);
                List<String> existingNames =
                    documentMapper.selectExistNamesIncludeDelete(documentNames);
                Collection<String> subtract = CollUtil.subtract(documentNames, existingNames);
                if (subtract.size() > 0) {
                    return subtract.stream().findFirst().get();
                }
            }
        }
    }

    @Override
    public List<String> getNamesByResourceIds(Collection<String> resourceIds) {
        return documentMapper.selectNameByResourceIdIn(resourceIds);
    }

    @Override
    public DocumentView getDocumentView(String documentName) {
        DocumentView view = new DocumentView();
        DocumentDTO document = documentMapper.selectByName(documentName);
        if (document == null) {
            return view;
        }
        view.setCreatedAt(document.getCreatedAt());
        view.setLastModifiedAt(document.getUpdatedAt());
        List<Long> userIds = new ArrayList<>();
        userIds.add(document.getCreatedBy());
        userIds.add(document.getUpdatedBy());
        List<MemberEntity> members =
            iMemberService.getByUserIdsAndSpaceId(userIds, document.getSpaceId());
        Map<Long, MemberEntity> userIdToMemberMap = members.stream()
            .collect(Collectors.toMap(MemberEntity::getUserId, Function.identity()));
        List<UserEntity> users = iUserService.listByIds(userIds);
        Map<Long, UserEntity> userIdToUserMap =
            users.stream().collect(Collectors.toMap(UserEntity::getId, Function.identity()));
        // append creator information
        UserEntity creator = Optional.ofNullable(userIdToUserMap.get(document.getCreatedBy()))
            .orElse(new UserEntity());
        view.setCreatorUuid(creator.getUuid());
        String creatorName = userIdToMemberMap.containsKey(document.getCreatedBy()) ?
            userIdToMemberMap.get(document.getCreatedBy()).getMemberName() :
            creator.getNickName();
        view.setCreatorName(creatorName);
        view.setCreatorAvatar(creator.getAvatar());
        if (document.getUpdatedBy() == null) {
            return view;
        }
        // append last modified by's information
        UserEntity LastModifiedBy = Optional
            .ofNullable(userIdToUserMap.get(document.getUpdatedBy()))
            .orElse(new UserEntity());
        view.setLastModifiedByUuid(LastModifiedBy.getUuid());
        String LastModifiedByName = userIdToMemberMap.containsKey(document.getUpdatedBy()) ?
            userIdToMemberMap.get(document.getUpdatedBy()).getMemberName() :
            LastModifiedBy.getNickName();
        view.setLastModifiedBy(LastModifiedByName);
        view.setLastModifiedByAvatar(LastModifiedBy.getAvatar());
        return view;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long userId, List<String> documentNames, DeleteWay deleteWay) {
        documentMapper.remove(documentNames, userId, deleteWay.name().toLowerCase());
        iSpaceAssetService.updateIsDeletedByNodeIds(documentNames, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recover(Long userId, List<String> documentNames) {
        documentMapper.recover(documentNames, userId);
        iSpaceAssetService.updateIsDeletedByNodeIds(documentNames, false);
    }
}
