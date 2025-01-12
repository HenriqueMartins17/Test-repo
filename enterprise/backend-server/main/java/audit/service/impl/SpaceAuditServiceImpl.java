/*
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

package com.apitable.enterprise.audit.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.apitable.base.enums.ParameterException;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.core.util.SpringContextHolder;
import com.apitable.enterprise.audit.entity.SpaceAuditEntity;
import com.apitable.enterprise.audit.mapper.SpaceAuditMapper;
import com.apitable.enterprise.audit.model.SpaceAuditPageParamDTO;
import com.apitable.enterprise.audit.model.SpaceAuditPageVO;
import com.apitable.enterprise.audit.service.ISpaceAuditService;
import com.apitable.enterprise.audit.setting.AuditConfigLoader;
import com.apitable.interfaces.billing.facade.EntitlementServiceFacade;
import com.apitable.interfaces.billing.model.SubscriptionInfo;
import com.apitable.organization.dto.MemberBaseInfoDTO;
import com.apitable.organization.mapper.MemberMapper;
import com.apitable.organization.service.IUnitService;
import com.apitable.organization.vo.UnitInfoVo;
import com.apitable.shared.constants.AuditConstants;
import com.apitable.shared.util.page.PageHelper;
import com.apitable.shared.util.page.PageInfo;
import com.apitable.space.dto.SpaceAuditDTO;
import com.apitable.space.enums.AuditSpaceCategory;
import com.apitable.space.enums.SubscribeFunctionException;
import com.apitable.workspace.entity.NodeEntity;
import com.apitable.workspace.mapper.NodeMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * space audit service implement.
 */
@Slf4j
@Service
public class SpaceAuditServiceImpl extends ServiceImpl<SpaceAuditMapper, SpaceAuditEntity>
    implements ISpaceAuditService {


    private static final String DEFAULT_DESCEND_COLUMNS = "id";

    private static final List<String> showAudits = AuditConfigLoader.getConfig().entrySet().stream()
        .filter((entry) -> entry.getValue().isShowInAuditLog())
        .map(Entry::getKey).collect(Collectors.toList());

    @Resource
    private EntitlementServiceFacade entitlementServiceFacade;

    @Resource
    private NodeMapper nodeMapper;

    @Resource
    private SpaceAuditMapper spaceAuditMapper;

    @Override
    public PageInfo<SpaceAuditPageVO> getSpaceAuditPageVO(String spaceId,
                                                          SpaceAuditPageParamDTO param) {
        // Gets the number of days the space subscription plan is available for audit query
        SubscriptionInfo subscriptionInfo = entitlementServiceFacade.getSpaceSubscription(spaceId);
        var auditQueryDays =
            Optional.ofNullable(subscriptionInfo.getFeature().getAuditQueryDays());
        long queryDays = 730L;
        if (auditQueryDays.isPresent() && !auditQueryDays.get().isUnlimited()) {
            queryDays = Optional.ofNullable(auditQueryDays.get().getValue()).orElse(0L);
        }
        if (queryDays == 0) {
            return PageHelper.build(param.getPageNo(), param.getPageSize(), 0,
                new ArrayList<>());
        }
        LocalDateTime today = LocalDateTimeUtil.beginOfDay(LocalDateTime.now());
        LocalDateTime beginTime = param.getBeginTime();
        // check start time
        if (beginTime != null) {
            long between = LocalDateTimeUtil.between(beginTime, today, ChronoUnit.DAYS);
            ExceptionUtil.isTrue(queryDays >= between, SubscribeFunctionException.AUDIT_LIMIT);
        } else {
            beginTime = today.plusDays(1 - queryDays);
            param.setBeginTime(beginTime);
        }
        // check end time
        if (param.getEndTime() != null) {
            ExceptionUtil.isFalse(
                LocalDateTimeUtil.between(beginTime, param.getEndTime()).isNegative(),
                ParameterException.INCORRECT_ARG);
        }

        // file search
        String likeName = StrUtil.trim(param.getKeyword());
        if (StrUtil.isNotBlank(likeName)) {
            // fuzzy search node
            List<String> nodeIds =
                nodeMapper.selectNodeIdBySpaceIdAndNodeNameLikeIncludeDeleted(spaceId, likeName);
            // The result is empty, ending the return
            if (nodeIds.isEmpty()) {
                return PageHelper.build(param.getPageNo(), param.getPageSize(), 0,
                    new ArrayList<>());
            }
            param.setNodeIds(nodeIds);
        }

        if (CollUtil.isEmpty(param.getActions()) && !showAudits.isEmpty()) {
            param.setActions(showAudits);
        }

        return getSpaceAuditPage(spaceId, param);
    }

    @Override
    public void createSpaceAuditRecord(SpaceAuditEntity entity) {
        spaceAuditMapper.insertEntity(entity);
    }

    protected PageInfo<SpaceAuditPageVO> getSpaceAuditPage(String spaceId,
                                                           SpaceAuditPageParamDTO param) {
        Page<SpaceAuditEntity> page = new Page<>(param.getPageNo(), param.getPageSize());
        page.addOrder(OrderItem.descs(DEFAULT_DESCEND_COLUMNS));
        IPage<SpaceAuditEntity> result =
            spaceAuditMapper.selectSpaceAuditPage(page, spaceId, param);
        if (result.getTotal() == 0) {
            return PageHelper.build(param.getPageNo(), param.getPageSize(), 0, new ArrayList<>());
        }
        List<SpaceAuditDTO> audits =
            result.getRecords().stream().map(i -> BeanUtil.copyProperties(i, SpaceAuditDTO.class))
                .collect(Collectors.toList());
        List<SpaceAuditPageVO> records = buildSpaceAuditPageVO(spaceId, audits);
        return new PageInfo<>(param.getPageNo(), param.getPageSize(), (int) result.getTotal(),
            records);
    }

    protected List<SpaceAuditPageVO> buildSpaceAuditPageVO(String spaceId,
                                                           List<SpaceAuditDTO> audits) {
        Set<Long> operatorMemberIds = new HashSet<>();
        Set<Long> unitIds = new HashSet<>();
        Set<String> nodeIds = new HashSet<>();
        // Iterate audits, record object id
        for (SpaceAuditDTO audit : audits) {
            operatorMemberIds.add(audit.getMemberId());
            JSONObject info = JSONUtil.parseObj(audit.getInfo());
            if (info.containsKey(AuditConstants.UNIT_IDS)) {
                unitIds.addAll(info.getJSONArray(AuditConstants.UNIT_IDS).toList(Long.class));
            } else if (info.containsKey(AuditConstants.UNIT_ID)) {
                unitIds.add(info.getLong(AuditConstants.UNIT_ID));
            }
            if (info.containsKey(AuditConstants.NODE_ID)) {
                nodeIds.add(info.getStr(AuditConstants.NODE_ID));
            }
        }

        // Batch querying information about members, organization units, and nodes
        List<MemberBaseInfoDTO> members = SpringContextHolder.getBean(MemberMapper.class)
            .selectBaseInfoDTOByIds(operatorMemberIds);
        Map<Long, MemberBaseInfoDTO> memberIdToDTOMap =
            members.stream().collect(Collectors.toMap(MemberBaseInfoDTO::getId, dto -> dto));
        Map<Long, UnitInfoVo> unitMap = new HashMap<>();
        if (!unitIds.isEmpty()) {
            List<UnitInfoVo> unitInfoList = SpringContextHolder.getBean(IUnitService.class)
                .getUnitInfoList(spaceId, new ArrayList<>(unitIds));
            unitMap =
                unitInfoList.stream().collect(Collectors.toMap(UnitInfoVo::getUnitId, vo -> vo));
        }
        Map<String, NodeEntity> nodeMap = new HashMap<>();
        if (!nodeIds.isEmpty()) {
            List<NodeEntity> nodeEntities = SpringContextHolder.getBean(NodeMapper.class)
                .selectByNodeIdsIncludeDeleted(nodeIds);
            nodeMap = nodeEntities.stream()
                .collect(Collectors.toMap(NodeEntity::getNodeId, node -> node));
        }

        // Build page view
        return buildAuditPageViews(spaceId, audits, memberIdToDTOMap, unitMap, nodeMap);
    }

    private List<SpaceAuditPageVO> buildAuditPageViews(String spaceId, List<SpaceAuditDTO> audits,
                                                       Map<Long, MemberBaseInfoDTO> memberIdToDTOMap,
                                                       Map<Long, UnitInfoVo> unitMap,
                                                       Map<String, NodeEntity> nodeMap) {
        List<SpaceAuditPageVO> vos = new ArrayList<>();
        for (SpaceAuditDTO audit : audits) {
            SpaceAuditPageVO vo = new SpaceAuditPageVO();
            vo.setCreatedAt(audit.getCreatedAt());
            vo.setAction(audit.getAction());
            // Build operator info
            SpaceAuditPageVO.Operator operator = new SpaceAuditPageVO.Operator();
            BeanUtil.copyProperties(memberIdToDTOMap.get(audit.getMemberId()), operator);
            operator.setMemberId(audit.getMemberId());
            vo.setOperator(operator);
            // Build audit info
            SpaceAuditPageVO.AuditContent content = new SpaceAuditPageVO.AuditContent();
            JSONObject info = JSONUtil.parseObj(audit.getInfo());
            AuditSpaceCategory category = AuditSpaceCategory.toEnum(audit.getCategory());
            switch (category) {
                case SPACE_CHANGE_EVENT:
                    SpaceAuditPageVO.Space space = new SpaceAuditPageVO.Space();
                    BeanUtil.copyProperties(audit.getInfo(), space);
                    space.setSpaceId(spaceId);
                    content.setSpace(space);
                    break;
                case WORK_CATALOG_CHANGE_EVENT:
                case WORK_CATALOG_SHARE_EVENT:
                case WORK_CATALOG_PERMISSION_CHANGE_EVENT:
                    // Append node info
                    appendNodeInfo(content, nodeMap, info);
                    // Append unit and control info
                    appendUnitAndControlInfo(content, unitMap, info);
                    break;
                case SPACE_TEMPLATE_EVENT:
                    SpaceAuditPageVO.Template template = new SpaceAuditPageVO.Template();
                    BeanUtil.copyProperties(audit.getInfo(), template);
                    content.setTemplate(template);
                    break;
                default:
                    break;
            }
            vo.setBody(content);
            vos.add(vo);
        }
        return vos;
    }

    private void appendNodeInfo(SpaceAuditPageVO.AuditContent content,
                                Map<String, NodeEntity> nodeMap, JSONObject info) {
        SpaceAuditPageVO.Node node = new SpaceAuditPageVO.Node();
        BeanUtil.copyProperties(info, node);
        NodeEntity nodeEntity = nodeMap.get(info.getStr(AuditConstants.NODE_ID));
        node.setCurrentNodeIcon(nodeEntity.getIcon());
        node.setCurrentNodeName(nodeEntity.getNodeName());
        content.setNode(node);
    }

    private void appendUnitAndControlInfo(SpaceAuditPageVO.AuditContent content,
                                          Map<Long, UnitInfoVo> unitMap, JSONObject info) {
        if (!info.containsKey(AuditConstants.UNIT_IDS)
            && !info.containsKey(AuditConstants.UNIT_ID)) {
            return;
        }
        List<Long> ids = info.containsKey(AuditConstants.UNIT_IDS)
            ? info.getJSONArray(AuditConstants.UNIT_IDS).toList(Long.class) :
            Collections.singletonList(info.getLong(AuditConstants.UNIT_ID));
        List<SpaceAuditPageVO.Unit> units = new ArrayList<>();
        for (Long unitId : ids) {
            SpaceAuditPageVO.Unit unit = new SpaceAuditPageVO.Unit();
            BeanUtil.copyProperties(unitMap.get(unitId), unit);
            units.add(unit);
        }
        content.setUnits(units);
        // Append control info
        SpaceAuditPageVO.Control control = new SpaceAuditPageVO.Control();
        BeanUtil.copyProperties(info, control);
        content.setControl(control);
    }

}
