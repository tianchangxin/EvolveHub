package org.evolve.admin.kb.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import org.evolve.common.base.BaseEntity;

/**
 * 知识库切片实体类
 *
 * @author tianchangxin
 * @date 2026/4/10
 */
@Getter
@Setter
@TableName("eh_kb_chunk")
public class KbChunkEntity extends BaseEntity {

    /**
     * 切片 UUID（全局唯一）
     */
    private String chunkId;

    /**
     * 所属文档 ID
     */
    private Long docId;

    /**
     * 所属知识库 ID
     */
    private Long kbId;

    /**
     * 切片序号（同一文档内）
     */
    private Integer chunkIndex;

    /**
     * 切片文本内容
     */
    private String content;

    /**
     * Token 数量
     */
    private Integer tokenCount;

    /**
     * 页码（PDF）
     */
    private Integer pageNum;

    /**
     * 标题路径（如：h1 > h2 > h3）
     */
    private String headingPath;

    /**
     * 切片类型：TEXT / TABLE / HEADING
     */
    private String chunkType;

    /**
     * 处理状态：PENDING / INDEXED / FAILED
     */
    private String status;

    /**
     * Milvus 向量 ID
     */
    private String milvusId;

    /**
     * 错误信息
     */
    private String errorMessage;
}