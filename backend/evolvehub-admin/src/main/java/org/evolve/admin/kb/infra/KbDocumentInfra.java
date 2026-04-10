package org.evolve.admin.kb.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.admin.kb.model.KbDocumentEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 知识库文档数据访问层
 *
 * @author tianchangxin
 * @date 2026/4/10
 */
@Repository
public class KbDocumentInfra extends ServiceImpl<KbDocumentInfra.KbDocumentMapper, KbDocumentEntity> {

    @Mapper
    interface KbDocumentMapper extends BaseMapper<KbDocumentEntity> {}

    /**
     * 根据知识库 ID 查询文档列表
     */
    public List<KbDocumentEntity> listByKbId(Long kbId) {
        return this.lambdaQuery().eq(KbDocumentEntity::getKbId, kbId).list();
    }

    /**
     * 根据主键查询文档
     */
    public KbDocumentEntity getById(Long id) {
        return super.getById(id);
    }

    /**
     * 统计知识库文档数量
     */
    public long countByKbId(Long kbId) {
        return this.lambdaQuery().eq(KbDocumentEntity::getKbId, kbId).count();
    }

    /**
     * 创建文档记录
     */
    public void createDocument(KbDocumentEntity entity) {
        this.save(entity);
    }

    /**
     * 更新文档
     */
    public void updateDocument(KbDocumentEntity entity) {
        this.updateById(entity);
    }

    /**
     * 删除文档
     */
    public void deleteDocument(Long id) {
        this.removeById(id);
    }

    /**
     * 批量删除知识库下的所有文档
     */
    public void deleteByKbId(Long kbId) {
        this.lambdaUpdate().eq(KbDocumentEntity::getKbId, kbId).remove();
    }

    /**
     * 分页查询文档列表
     */
    public Page<KbDocumentEntity> listPageByKbId(Long kbId, int pageNum, int pageSize) {
        return this.lambdaQuery()
                .eq(KbDocumentEntity::getKbId, kbId)
                .page(new Page<>(pageNum, pageSize));
    }
}