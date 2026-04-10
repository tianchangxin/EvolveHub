package org.evolve.admin.kb.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

/**
 * 知识库实体类
 *
 * @author tianchangxin
 * @date 2026/4/10
 */
@Getter
@Setter
@TableName("eh_knowledge_base")
public class KnowledgeBaseEntity extends BaseEntity {

    /**
     * 知识库名称
     */
    private String name;

    /**
     * 知识库层级：GLOBAL / DEPT / PROJECT / SENSITIVE
     */
    private String level;

    /**
     * 所属部门 ID（DEPT / PROJECT 级别必填）
     */
    private Long deptId;

    /**
     * 拥有者 ID（创建人）
     */
    private Long ownerId;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态（0-禁用 1-正常）
     */
    private Integer status;
}