package org.evolve.admin.kb.service;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文档切片器（参考 MinerU 版面分析逻辑）
 * <p>
 * 切片策略：
 * 1. 按标题层级切分大块（h1/h2/h3 作为自然分界）
 * 2. 大块内按段落切分
 * 3. 超长段落按 token 滑动窗口切
 * 4. 表格单独作为一个切片
 *
 * @author tianchangxin
 * @date 2026/4/10
 */
@Slf4j
@Component
public class DocumentChunker {

    /**
     * 切片大小（token）
     */
    private static final int CHUNK_SIZE = 512;

    /**
     * 切片重叠（token）
     */
    private static final int CHUNK_OVERLAP = 64;

    /**
     * 标题正则（匹配 Markdown 标题）
     */
    private static final Pattern HEADING_PATTERN = Pattern.compile("^(#{1,6})\\s+(.+)$", Pattern.MULTILINE);

    /**
     * 表格正则（简单匹配 | 开头的行）
     */
    private static final Pattern TABLE_PATTERN = Pattern.compile("^\\|.*\\|$", Pattern.MULTILINE);

    /**
     * 对文档内容进行切片
     *
     * @param content  文档内容
     * @param fileName 文件名
     * @return 切片列表
     */
    public List<Chunk> chunk(String content, String fileName) {
        if (StrUtil.isBlank(content)) {
            return new ArrayList<>();
        }

        List<Chunk> chunks = new ArrayList<>();
        int chunkIndex = 0;

        // 1. 按标题切分大块
        List<Section> sections = splitByHeading(content);

        // 2. 对每个大块进行进一步切分
        for (Section section : sections) {
            // 表格独立成切片
            if (isTable(section.getContent())) {
                Chunk chunk = new Chunk();
                chunk.setChunkIndex(chunkIndex++);
                chunk.setContent(section.getContent().trim());
                chunk.setTokenCount(estimateTokens(chunk.getContent()));
                chunk.setHeadingPath(section.getHeadingPath());
                chunk.setChunkType("TABLE");
                chunks.add(chunk);
                continue;
            }

            // 按段落切分
            String[] paragraphs = section.getContent().split("\n\n+");
            StringBuilder buffer = new StringBuilder();

            for (String para : paragraphs) {
                para = para.trim();
                if (para.isEmpty()) continue;

                int paraTokens = estimateTokens(para);

                // 如果单个段落超长，滑动窗口切分
                if (paraTokens > CHUNK_SIZE) {
                    // 先保存 buffer
                    if (buffer.length() > 0) {
                        Chunk chunk = createChunk(buffer.toString(), chunkIndex++, section.getHeadingPath());
                        chunks.add(chunk);
                        buffer.setLength(0);
                    }

                    // 滑动窗口切分超长段落
                    List<Chunk> paraChunks = slidingWindowChunk(para, chunkIndex, section.getHeadingPath());
                    chunks.addAll(paraChunks);
                    chunkIndex += paraChunks.size();
                    continue;
                }

                // 如果加上这个段落会超长，先保存 buffer
                if (estimateTokens(buffer.toString()) + paraTokens > CHUNK_SIZE && buffer.length() > 0) {
                    Chunk chunk = createChunk(buffer.toString(), chunkIndex++, section.getHeadingPath());
                    chunks.add(chunk);
                    buffer.setLength(0);
                }

                // 加入 buffer
                if (buffer.length() > 0) {
                    buffer.append("\n\n");
                }
                buffer.append(para);
            }

            // 保存最后的 buffer
            if (buffer.length() > 0) {
                Chunk chunk = createChunk(buffer.toString(), chunkIndex++, section.getHeadingPath());
                chunks.add(chunk);
            }
        }

        log.info("文档切片完成: fileName={}, chunkCount={}", fileName, chunks.size());
        return chunks;
    }

    /**
     * 按标题切分大块
     */
    private List<Section> splitByHeading(String content) {
        List<Section> sections = new ArrayList<>();
        Matcher matcher = HEADING_PATTERN.matcher(content);

        int lastEnd = 0;
        String currentHeading = null;

        while (matcher.find()) {
            // 保存上一块
            if (lastEnd < matcher.start()) {
                String sectionContent = content.substring(lastEnd, matcher.start()).trim();
                if (!sectionContent.isEmpty()) {
                    Section section = new Section();
                    section.setContent(sectionContent);
                    section.setHeadingPath(currentHeading);
                    sections.add(section);
                }
            }

            // 更新当前标题
            currentHeading = matcher.group(2).trim();
            lastEnd = matcher.end();
        }

        // 最后一块
        if (lastEnd < content.length()) {
            String sectionContent = content.substring(lastEnd).trim();
            if (!sectionContent.isEmpty()) {
                Section section = new Section();
                section.setContent(sectionContent);
                section.setHeadingPath(currentHeading);
                sections.add(section);
            }
        }

        // 如果没有标题，整个文档作为一块
        if (sections.isEmpty()) {
            Section section = new Section();
            section.setContent(content);
            section.setHeadingPath(null);
            sections.add(section);
        }

        return sections;
    }

    /**
     * 滑动窗口切分超长段落
     */
    private List<Chunk> slidingWindowChunk(String text, int startIndex, String headingPath) {
        List<Chunk> chunks = new ArrayList<>();
        String[] words = text.split("\\s+");
        int totalWords = words.length;

        int start = 0;
        int chunkIndex = startIndex;

        while (start < totalWords) {
            int end = Math.min(start + CHUNK_SIZE, totalWords);
            StringBuilder chunkContent = new StringBuilder();

            for (int i = start; i < end; i++) {
                if (i > start) chunkContent.append(" ");
                chunkContent.append(words[i]);
            }

            Chunk chunk = createChunk(chunkContent.toString(), chunkIndex++, headingPath);
            chunks.add(chunk);

            // 滑动窗口：下一个起点往前移 overlap
            start = end - CHUNK_OVERLAP;
            if (start >= totalWords) break;
        }

        return chunks;
    }

    /**
     * 创建切片对象
     */
    private Chunk createChunk(String content, int index, String headingPath) {
        Chunk chunk = new Chunk();
        chunk.setChunkIndex(index);
        chunk.setContent(content.trim());
        chunk.setTokenCount(estimateTokens(chunk.getContent()));
        chunk.setHeadingPath(headingPath);
        chunk.setChunkType("TEXT");
        return chunk;
    }

    /**
     * 判断是否为表格
     */
    private boolean isTable(String content) {
        Matcher matcher = TABLE_PATTERN.matcher(content);
        return matcher.find();
    }

    /**
     * 估算 token 数量（简化：按单词数估算，实际应调用 tokenizer）
     */
    private int estimateTokens(String text) {
        if (StrUtil.isBlank(text)) return 0;
        // 中文按字符数 * 0.5，英文按空格分词
        int chineseChars = text.replaceAll("[^\\u4e00-\\u9fa5]", "").length();
        int englishWords = text.split("\\s+").length;
        return (int) (chineseChars * 0.5 + englishWords);
    }

    /**
     * 切片数据结构
     */
    @Data
    public static class Chunk {
        private Integer chunkIndex;
        private String content;
        private Integer tokenCount;
        private String headingPath;
        private String chunkType; // TEXT / TABLE / HEADING
        private Integer pageNum; // PDF 页码（暂未实现）
    }

    /**
     * 章节数据结构
     */
    @Data
    private static class Section {
        private String content;
        private String headingPath;
    }
}