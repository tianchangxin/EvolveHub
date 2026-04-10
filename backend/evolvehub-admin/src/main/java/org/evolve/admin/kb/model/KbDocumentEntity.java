package org.evolve.admin.kb.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

/**
 * 知识库文档实体类
 *
 * @author tianchangxin
 * @date 2026/4/10
 */
@Getter
@Setter
@TableName("eh_kb_document")
public class KbDocumentEntity extends BaseEntity {

    /**
     * 所属知识库 ID
     */
    private Long kbId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件路径（MinIO 对象 key）
     */
    private String filePath;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件类型（pdf / docx / txt / md）
     */
    private String fileType;

    /**
     * 切片数量
     */
    private Integer chunkCount;

    /**
     * 处理状态：PENDING / PROCESSING / READY / FAILED
     */
    private String status;

    /**
     * 错误信息
     */
    private String errorMessage;
}