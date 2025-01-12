package com.apitable.enterprise.airagent.service.impl;

import static com.apitable.shared.util.IdUtil.createNodeId;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.apitable.core.exception.BusinessException;
import com.apitable.enterprise.airagent.entity.AgentEntity;
import com.apitable.enterprise.airagent.enums.AirAgentException;
import com.apitable.enterprise.airagent.mapper.AgentMapper;
import com.apitable.enterprise.airagent.model.AgentCreateRO;
import com.apitable.enterprise.airagent.model.AgentDTO;
import com.apitable.enterprise.airagent.model.AgentUpdateParams;
import com.apitable.enterprise.airagent.model.AiAgent;
import com.apitable.enterprise.airagent.model.SortedAgents;
import com.apitable.enterprise.airagent.service.IAgentService;
import com.apitable.enterprise.airagent.service.IDataSourceService;
import com.apitable.interfaces.ai.facade.AiServiceFacade;
import com.apitable.interfaces.ai.model.AiCreateParam;
import com.apitable.interfaces.ai.model.AiUpdateParam;
import com.apitable.shared.sysconfig.i18n.I18nStringsUtil;
import com.apitable.workspace.enums.NodeType;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.List;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * agent service implements.
 */
@Service
@Slf4j
public class AgentServiceImpl extends ServiceImpl<AgentMapper, AgentEntity>
    implements IAgentService {

    @Resource
    private AiServiceFacade aiServiceFacade;

    @Resource
    private IDataSourceService iDataSourceService;

    @Override
    public SortedAgents getUserAgents(Long userId) {
        List<AgentDTO> agentDtoList = baseMapper.selectByUserId(userId);
        SortedAgents aiAgents = new SortedAgents();
        agentDtoList.forEach(dto -> aiAgents.add(new AiAgent(dto)));
        aiAgents.sort();
        return aiAgents;
    }

    @Override
    public AiAgent getAgent(String agentId) {
        AgentDTO agentDto = baseMapper.selectByAgentId(agentId);
        if (agentDto == null) {
            return null;
        }
        return new AiAgent(agentDto);
    }

    @Override
    public void checkAgent(String agentId) {
        AgentDTO agentDto = baseMapper.selectByAgentId(agentId);
        if (agentDto == null) {
            throw new BusinessException(AirAgentException.AGENT_NOT_FOUND);
        }
    }

    @Override
    public AgentEntity getEntityByAgentId(String agentId) {
        return getOne(new QueryWrapper<AgentEntity>().eq("agent_id", agentId), false);
    }

    @Override
    public AgentEntity getEntityByPreAgentId(String preAgentId) {
        return getOne(new QueryWrapper<AgentEntity>().eq("pre_agent_id", preAgentId));
    }


    @Override
    public AgentEntity getTopAgent(Long userId) {
        return baseMapper.selectTopAgentByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String create(Long userId, AgentCreateRO param) {
        if (param == null) {
            param = new AgentCreateRO();
        }
        String name =
            StrUtil.blankToDefault(param.getName(), I18nStringsUtil.t("default_create_airagent"));
        name = addSuffixIfDuplicateName(userId, name);
        String agentId = createNodeId(NodeType.AIRAGENT);
        aiServiceFacade.createAi(AiCreateParam.builder()
            .aiId(agentId)
            .aiName(name)
            .build()
        );
        String preAgentId = param.getPreAgentId();
        if (StrUtil.isBlank(preAgentId)) {
            // add top sequence
            AgentEntity topAgent = getTopAgent(userId);
            if (topAgent != null) {
                // move top agent to next
                updatePreAgentIdById(topAgent.getId(), agentId);
            }
        } else {
            // add middle sequence or last sequence
            AgentEntity preAgent = getEntityByAgentId(preAgentId);
            if (preAgent == null) {
                throw new BusinessException(AirAgentException.AGENT_NOT_FOUND);
            }
            AgentEntity nextAgent = getEntityByPreAgentId(preAgentId);
            if (nextAgent != null) {
                // move next agent to next
                updatePreAgentIdById(nextAgent.getId(), agentId);
            }

        }
        AgentEntity entity = AgentEntity.builder()
            .aiId(agentId)
            .agentId(agentId)
            .agentName(name)
            .preAgentId(StrUtil.blankToDefault(preAgentId, null))
            .build();
        save(entity);
        return agentId;
    }

    @Override
    public void updatePreAgentIdById(Long id, String preAgentId) {
        // update order
        AgentEntity updateEntity = new AgentEntity();
        updateEntity.setId(id);
        updateEntity.setPreAgentId(preAgentId);
        updateById(updateEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(String agentId, AgentUpdateParams updateParams) {
        AgentEntity agentEntity = getEntityByAgentId(agentId);
        if (agentEntity == null) {
            throw new BusinessException(AirAgentException.AGENT_NOT_FOUND);
        }

        boolean updateAgent = false;
        AgentEntity updateEntity = new AgentEntity();

        if (StringUtils.hasText(updateParams.getName())
            && !updateParams.getName().equals(agentEntity.getAgentName())) {
            updateEntity.setAgentName(updateParams.getName());
            updateAgent = true;
        }

        if (MapUtil.isNotEmpty(updateParams.getSetting())) {
            JSONObject setting = JSONUtil.parseObj(updateParams.getSetting());
            updateEntity.setSetting(setting.toJSONString(0));
            updateAgent = true;
        }

        if (updateAgent) {
            updateEntity.setId(agentEntity.getId());
            updateById(updateEntity);
            // sync update ai
            aiServiceFacade.updateAi(agentId, AiUpdateParam.builder()
                .name(updateEntity.getAgentName())
                .setting(JSONUtil.parseObj(updateParams.getSetting()))
                .build());
        }

        // check data source update available
        if (!CollectionUtils.isEmpty(updateParams.getDataSources())) {
            iDataSourceService.addDataSources(agentId, updateParams.getDataSources());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, String agentId) {
        AgentEntity agentEntity = getEntityByAgentId(agentId);
        if (agentEntity == null) {
            throw new BusinessException(AirAgentException.AGENT_NOT_FOUND);
        }
        // update preAgentId of next agent
        AgentEntity nextAgent = getEntityByPreAgentId(agentId);
        if (nextAgent != null) {
            baseMapper.updatePreAgentIdById(nextAgent.getId(), agentEntity.getPreAgentId(), userId);
        }

        // delete
        removeById(agentEntity.getId());
        // delete ai
        aiServiceFacade.deleteAi(ListUtil.toList(agentId));
    }

    /**
     * increase if duplicate name.
     *
     * @param userId    user id
     * @param agentName name
     * @return String
     */
    private String addSuffixIfDuplicateName(Long userId, String agentName) {
        String duplicateName = baseMapper.selectAgentNameByUserIdAndName(userId, agentName);
        if (StrUtil.isBlank(duplicateName)) {
            return agentName;
        }
        if (duplicateName.length() == agentName.length()) {
            return agentName.concat(" 1");
        }
        String suffix = duplicateName.substring(agentName.length() + 1);
        if (NumberUtil.isNumber(suffix)) {
            int i = Integer.parseInt(suffix);
            return agentName.concat(" " + (i + 1));
        }
        return agentName.concat(" 1");
    }
}
