package org.evolve.admin.kb.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库响应
 *
 * @author tianchangxin
 * @date 2026/4/10
 */
@Data
public class KbResponse {
    private Long id;
    private String name;
    private String level;
    private Long deptId;
    private Long ownerId;
    private String description;
    private Integer status;
    private Long documentCount;
    private LocalDateTime createTime;
}
