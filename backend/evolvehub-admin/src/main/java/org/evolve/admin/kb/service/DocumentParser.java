package org.evolve.admin.kb.service;

import cn.hutool.core.io.IoUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

/**
 * 文档解析器（Apache Tika）
 * <p>
 * 支持 pdf / docx / txt / md 格式，统一提取文本内容
 * 保留标题层级、段落结构和表格标记，供 DocumentChunker 使用
 *
 * @author tianchangxin
 * @date 2026/4/10
 */
@Slf4j
@Component
public class DocumentParser {

    /**
     * 解析文档，提取文本内容
     *
     * @param inputStream 文件输入流
     * @param fileName    文件名（用于判断格式）
     * @return 解析后的文本内容
     * @throws DocumentParseException 解析失败时抛出
     */
    public String parse(InputStream inputStream, String fileName) {
        try {
            // Tika 自动检测格式
            AutoDetectParser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler(-1); // 不限制文本长度
            Metadata metadata = new Metadata();
            metadata.set(Metadata.RESOURCE_NAME_KEY, fileName);
            ParseContext context = new ParseContext();

            parser.parse(inputStream, handler, metadata, context);
            String content = handler.toString();

            log.info("文档解析成功: fileName={}, contentLength={}", fileName, content.length());
            return content;

        } catch (IOException | SAXException | TikaException e) {
            log.error("文档解析失败: fileName={}", fileName, e);
            throw new DocumentParseException("文档解析失败: " + e.getMessage(), e);
        } finally {
            IoUtil.close(inputStream);
        }
    }

    /**
     * 校验文件格式是否支持
     *
     * @param fileName 文件名
     * @return true-支持 false-不支持
     */
    public boolean isSupportedFormat(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return false;
        }
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return "pdf".equals(extension)
                || "docx".equals(extension)
                || "txt".equals(extension)
                || "md".equals(extension);
    }

    /**
     * 文档解析异常
     */
    public static class DocumentParseException extends RuntimeException {
        public DocumentParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}