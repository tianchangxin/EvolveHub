package org.evolve.admin.kb.service;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.evolve.admin.config.StorageConfig;
import org.evolve.admin.kb.infra.KbChunkInfra;
import org.evolve.admin.kb.infra.KbDocumentInfra;
import org.evolve.admin.kb.infra.KnowledgeBaseInfra;
import org.evolve.admin.kb.model.KbChunkEntity;
import org.evolve.admin.kb.model.KbDocumentEntity;
import org.evolve.admin.kb.model.KnowledgeBaseEntity;
import org.evolve.admin.kb.response.DocumentResponse;
import org.evolve.common.web.exception.BusinessException;
import org.evolve.common.web.response.ResultCode;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文档上传与处理流水线
 *
 * @author tianchangxin
 * @date 2026/4/10
 */
@Slf4j
@Service
public class DocumentUploadManager {

    @Resource
    private KnowledgeBaseInfra kbInfra;

    @Resource
    private KbDocumentInfra kbDocumentInfra;

    @Resource
    private KbChunkInfra kbChunkInfra;

    @Resource
    private MinioClient minioClient;

    @Resource
    private StorageConfig.MinioProperties minioProperties;

    @Resource
    private DocumentParser documentParser;

    @Resource
    private DocumentChunker documentChunker;

    /**
     * 上传文档
     *
     * @param file 文件
     * @param kbId 知识库ID
     * @return 文档信息
     */
    @Transactional(rollbackFor = Exception.class)
    public DocumentResponse upload(MultipartFile file, Long kbId) {
        // 1. 校验知识库存在
        KnowledgeBaseEntity kb = kbInfra.getById(kbId);
        if (kb == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "知识库不存在");
        }

        // 2. 校验文件格式
        String fileName = file.getOriginalFilename();
        if (!documentParser.isSupportedFormat(fileName)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不支持的文件格式，仅支持 pdf/docx/txt/md");
        }

        // 3. 上传到 MinIO
        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
        String objectKey = "kb/" + kbId + "/" + IdUtil.fastSimpleUUID() + "." + fileType;

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectKey)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception e) {
            log.error("文件上传到 MinIO 失败: fileName={}", fileName, e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "文件上传失败");
        }

        // 4. 创建文档记录
        KbDocumentEntity document = new KbDocumentEntity();
        document.setKbId(kbId);
        document.setFileName(fileName);
        document.setFilePath(objectKey);
        document.setFileSize(file.getSize());
        document.setFileType(fileType);
        document.setChunkCount(0);
        document.setStatus("PENDING");

        kbDocumentInfra.createDocument(document);

        log.info("文档上传成功: docId={}, fileName={}, kbId={}", document.getId(), fileName, kbId);

        // 5. 异步处理文档（解析 + 切片 + 向量化）
        processDocumentAsync(document.getId());

        DocumentResponse response = new DocumentResponse();
        response.setId(document.getId());
        response.setKbId(document.getKbId());
        response.setFileName(document.getFileName());
        response.setFileType(document.getFileType());
        response.setFileSize(document.getFileSize());
        response.setChunkCount(0);
        response.setStatus(document.getStatus());
        response.setCreateTime(document.getCreateTime());

        return response;
    }

    /**
     * 异步处理文档流水线
     */
    @Async
    public void processDocumentAsync(Long docId) {
        try {
            log.info("开始处理文档: docId={}", docId);

            // 更新状态为处理中
            KbDocumentEntity document = kbDocumentInfra.getById(docId);
            document.setStatus("PROCESSING");
            kbDocumentInfra.updateDocument(document);

            // 1. 从 MinIO 下载文件
            InputStream inputStream = minioClient.getObject(
                    io.minio.GetObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(document.getFilePath())
                            .build()
            );

            // 2. Tika 解析
            String content = documentParser.parse(inputStream, document.getFileName());

            // 3. 文档切片
            List<DocumentChunker.Chunk> chunks = documentChunker.chunk(content, document.getFileName());

            // 4. 保存切片到 PostgreSQL
            List<KbChunkEntity> chunkEntities = chunks.stream().map(chunk -> {
                KbChunkEntity entity = new KbChunkEntity();
                entity.setChunkId(IdUtil.fastSimpleUUID());
                entity.setDocId(document.getId());
                entity.setKbId(document.getKbId());
                entity.setChunkIndex(chunk.getChunkIndex());
                entity.setContent(chunk.getContent());
                entity.setTokenCount(chunk.getTokenCount());
                entity.setPageNum(chunk.getPageNum());
                entity.setHeadingPath(chunk.getHeadingPath());
                entity.setChunkType(chunk.getChunkType());
                entity.setStatus("PENDING");
                return entity;
            }).collect(Collectors.toList());

            kbChunkInfra.batchCreate(chunkEntities);

            // 5. TODO: 向量化并写入 Milvus（需要调用 Embedding 服务）
            // 此处简化，直接标记为 READY
            document.setStatus("READY");
            document.setChunkCount(chunks.size());
            kbDocumentInfra.updateDocument(document);

            log.info("文档处理成功: docId={}, chunkCount={}", docId, chunks.size());

        } catch (Exception e) {
            log.error("文档处理失败: docId={}", docId, e);

            KbDocumentEntity document = kbDocumentInfra.getById(docId);
            document.setStatus("FAILED");
            document.setErrorMessage(e.getMessage());
            kbDocumentInfra.updateDocument(document);
        }
    }
}
