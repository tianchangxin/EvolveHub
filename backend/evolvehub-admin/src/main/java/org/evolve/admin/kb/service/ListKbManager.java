package org.evolve.admin.kb.service;

import cn.hutool.core.collection.CollUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.evolve.admin.kb.infra.KbDocumentInfra;
import org.evolve.admin.kb.infra.KbPermissionInfra;
import org.evolve.admin.kb.infra.KnowledgeBaseInfra;
import org.evolve.admin.kb.model.KnowledgeBaseEntity;
import org.evolve.admin.kb.model.KbPermissionEntity;
import org.evolve.admin.kb.response.KbResponse;
import org.evolve.common.base.CurrentUserHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 知识库列表查询（按权限过滤）
 *
 * @author tianchangxin
 * @date 2026/4/10
 */
@Slf4j
@Service
public class ListKbManager {

    @Resource
    private KnowledgeBaseInfra kbInfra;

    @Resource
    private KbDocumentInfra kbDocumentInfra;

    @Resource
    private KbPermissionInfra kbPermissionInfra;

    /**
     * 查询当前用户可见的知识库列表
     *
     * @return 知识库列表
     */
    public List<KbResponse> listVisibleKb() {
        Long userId = CurrentUserHolder.getUserId();

        // TODO: 实际应该根据用户角色过滤（超管看全部、其他角色根据权限过滤）
        // 此处简化：直接返回所有知识库
        List<KnowledgeBaseEntity> kbList = kbInfra.list();

        // 统计每个知识库的文档数量
        Map<Long, Long> docCountMap = kbList.stream()
                .collect(Collectors.toMap(
                        KnowledgeBaseEntity::getId,
                        kb -> kbDocumentInfra.countByKbId(kb.getId())
                ));

        return kbList.stream().map(kb -> {
            KbResponse response = new KbResponse();
            response.setId(kb.getId());
            response.setName(kb.getName());
            response.setLevel(kb.getLevel());
            response.setDeptId(kb.getDeptId());
            response.setOwnerId(kb.getOwnerId());
            response.setDescription(kb.getDescription());
            response.setStatus(kb.getStatus());
            response.setDocumentCount(docCountMap.getOrDefault(kb.getId(), 0L));
            response.setCreateTime(kb.getCreateTime());
            return response;
        }).collect(Collectors.toList());
    }
}
