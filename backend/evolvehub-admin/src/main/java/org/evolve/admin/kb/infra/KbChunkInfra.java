package org.evolve.admin.kb.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.admin.kb.model.KbChunkEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 知识库切片数据访问层
 *
 * @author tianchangxin
 * @date 2026/4/10
 */
@Repository
public class KbChunkInfra extends ServiceImpl<KbChunkInfra.KbChunkMapper, KbChunkEntity> {

    @Mapper
    interface KbChunkMapper extends BaseMapper<KbChunkEntity> {}

    /**
     * 根据文档 ID 查询切片列表
     */
    public List<KbChunkEntity> listByDocId(Long docId) {
        return this.lambdaQuery().eq(KbChunkEntity::getDocId, docId).list();
    }

    /**
     * 根据知识库 ID 查询切片列表
     */
    public List<KbChunkEntity> listByKbId(Long kbId) {
        return this.lambdaQuery().eq(KbChunkEntity::getKbId, kbId).list();
    }

    /**
     * 批量创建切片
     */
    public void batchCreate(List<KbChunkEntity> chunks) {
        this.saveBatch(chunks);
    }

    /**
     * 更新切片
     */
    public void updateChunk(KbChunkEntity entity) {
        this.updateById(entity);
    }

    /**
     * 删除文档的所有切片
     */
    public void deleteByDocId(Long docId) {
        this.lambdaUpdate().eq(KbChunkEntity::getDocId, docId).remove();
    }

    /**
     * 删除知识库的所有切片
     */
    public void deleteByKbId(Long kbId) {
        this.lambdaUpdate().eq(KbChunkEntity::getKbId, kbId).remove();
    }

    /**
     * 统计文档的切片数量
     */
    public long countByDocId(Long docId) {
        return this.lambdaQuery().eq(KbChunkEntity::getDocId, docId).count();
    }
}