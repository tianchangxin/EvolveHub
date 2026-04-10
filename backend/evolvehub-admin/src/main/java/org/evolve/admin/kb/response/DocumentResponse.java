package org.evolve.admin.kb.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档响应
 *
 * @author tianchangxin
 * @date 2026/4/10
 */
@Data
public class DocumentResponse {
    private Long id;
    private Long kbId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private Integer chunkCount;
    private String status;
    private String errorMessage;
    private LocalDateTime createTime;
}
