package org.evolve.admin.kb.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建知识库请求
 *
 * @author tianchangxin
 * @date 2026/4/10
 */
@Data
public class CreateKbRequest {

    /**
     * 知识库名称
     */
    @NotBlank(message = "知识库名称不能为空")
    private String name;

    /**
     * 知识库层级：GLOBAL / DEPT / PROJECT / SENSITIVE
     */
    @NotBlank(message = "知识库层级不能为空")
    private String level;

    /**
     * 所属部门 ID（DEPT / PROJECT 级别必填）
     */
    private Long deptId;

    /**
     * 描述
     */
    private String description;
}
