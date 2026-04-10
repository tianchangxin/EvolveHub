package org.evolve.admin.kb.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

/**
 * 知识库权限实体类
 *
 * @author tianchangxin
 * @date 2026/4/10
 */
@Getter
@Setter
@TableName("eh_kb_permission")
public class KbPermissionEntity extends BaseEntity {

    /**
     * 知识库 ID
     */
    private Long kbId;

    /**
     * 授权对象类型：USER / DEPT / ROLE
     */
    private String targetType;

    /**
     * 授权对象 ID（用户 ID / 部门 ID / 角色 ID）
     */
    private Long targetId;

    /**
     * 权限类型：READ / WRITE
     */
    private String permissionType;
}