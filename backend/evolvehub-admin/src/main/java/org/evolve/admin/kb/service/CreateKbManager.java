package org.evolve.admin.kb.service;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.hutool.core.util.IdUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.evolve.admin.kb.infra.KnowledgeBaseInfra;
import org.evolve.admin.kb.infra.KbDocumentInfra;
import org.evolve.admin.kb.model.KnowledgeBaseEntity;
import org.evolve.admin.kb.request.CreateKbRequest;
import org.evolve.admin.kb.response.KbResponse;
import org.evolve.common.base.BaseManager;
import org.evolve.common.base.CurrentUserHolder;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.stereotype.Service;

/**
 * 创建知识库业务处理器
 *
 * @author tianchangxin
 * @date 2026/4/10
 */
@Slf4j
@Service
public class CreateKbManager extends BaseManager<CreateKbRequest, KbResponse> {

    @Resource
    private KnowledgeBaseInfra kbInfra;

    @Resource
    private KbDocumentInfra kbDocumentInfra;

    @Override
    protected void check(CreateKbRequest request) {
        // 知识库名称不能重复
        if (kbInfra.getByName(request.getName()) != null) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "知识库名称已存在");
        }

        // DEPT / PROJECT 级别必须填写 deptId
        if (("DEPT".equals(request.getLevel()) || "PROJECT".equals(request.getLevel()))
                && request.getDeptId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "部门级/项目级知识库必须指定部门");
        }
    }

    @Override
    protected KbResponse process(CreateKbRequest request) {
        KnowledgeBaseEntity entity = new KnowledgeBaseEntity();
        entity.setName(request.getName());
        entity.setLevel(request.getLevel());
        entity.setDeptId(request.getDeptId());
        entity.setOwnerId(CurrentUserHolder.getUserId());
        entity.setDescription(request.getDescription());
        entity.setStatus(1);

        kbInfra.createKb(entity);

        log.info("知识库创建成功: kbId={}, name={}", entity.getId(), entity.getName());

        KbResponse response = new KbResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setLevel(entity.getLevel());
        response.setDeptId(entity.getDeptId());
        response.setOwnerId(entity.getOwnerId());
        response.setDescription(entity.getDescription());
        response.setStatus(entity.getStatus());
        response.setDocumentCount(0L);
        response.setCreateTime(entity.getCreateTime());

        return response;
    }
}
