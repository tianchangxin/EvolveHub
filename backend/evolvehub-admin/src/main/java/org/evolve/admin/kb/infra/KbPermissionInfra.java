package org.evolve.admin.kb.infra;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.evolve.admin.kb.model.KbPermissionEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 知识库权限数据访问层
 *
 * @author tianchangxin
 * @date 2026/4/10
 */
@Repository
public class KbPermissionInfra extends ServiceImpl<KbPermissionInfra.KbPermissionMapper, KbPermissionEntity> {

    @Mapper
    interface KbPermissionMapper extends BaseMapper<KbPermissionEntity> {}

    /**
     * 根据知识库 ID 查询权限列表
     */
    public List<KbPermissionEntity> listByKbId(Long kbId) {
        return this.lambdaQuery().eq(KbPermissionEntity::getKbId, kbId).list();
    }

    /**
     * 根据知识库 ID 和目标查询权限
     */
    public KbPermissionEntity getByKbIdAndTarget(Long kbId, String targetType, Long targetId) {
        return this.lambdaQuery()
                .eq(KbPermissionEntity::getKbId, kbId)
                .eq(KbPermissionEntity::getTargetType, targetType)
                .eq(KbPermissionEntity::getTargetId, targetId)
                .one();
    }

    /**
     * 批量创建权限
     */
    public void batchCreate(List<KbPermissionEntity> permissions) {
        this.saveBatch(permissions);
    }

    /**
     * 删除权限
     */
    public void deletePermission(Long id) {
        this.removeById(id);
    }

    /**
     * 删除知识库的所有权限
     */
    public void deleteByKbId(Long kbId) {
        this.lambdaUpdate().eq(KbPermissionEntity::getKbId, kbId).remove();
    }
}