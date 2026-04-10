package org.evolve.admin.kb.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.admin.kb.model.KnowledgeBaseEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 知识库数据访问层
 *
 * @author tianchangxin
 * @date 2026/4/10
 */
@Repository
public class KnowledgeBaseInfra extends ServiceImpl<KnowledgeBaseInfra.KnowledgeBaseMapper, KnowledgeBaseEntity> {

    @Mapper
    interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBaseEntity> {}

    /**
     * 根据名称查询知识库（唯一性校验）
     */
    public KnowledgeBaseEntity getByName(String name) {
        return this.lambdaQuery().eq(KnowledgeBaseEntity::getName, name).one();
    }

    /**
     * 根据主键查询知识库
     */
    public KnowledgeBaseEntity getById(Long id) {
        return super.getById(id);
    }

    /**
     * 根据层级和部门查询知识库列表
     */
    public List<KnowledgeBaseEntity> listByLevelAndDeptId(String level, Long deptId) {
        return this.lambdaQuery()
                .eq(KnowledgeBaseEntity::getLevel, level)
                .eq(deptId != null, KnowledgeBaseEntity::getDeptId, deptId)
                .list();
    }

    /**
     * 创建知识库
     */
    public void createKb(KnowledgeBaseEntity entity) {
        this.save(entity);
    }

    /**
     * 更新知识库
     */
    public void updateKb(KnowledgeBaseEntity entity) {
        this.updateById(entity);
    }

    /**
     * 删除知识库
     */
    public void deleteKb(Long id) {
        this.removeById(id);
    }

    /**
     * 分页查询知识库列表
     */
    public Page<KnowledgeBaseEntity> listPage(int pageNum, int pageSize) {
        return this.page(new Page<>(pageNum, pageSize));
    }
}