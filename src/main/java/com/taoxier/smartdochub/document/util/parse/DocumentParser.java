package com.taoxier.smartdochub.document.util.parse;

import com.taoxier.smartdochub.document.model.entity.ContentChunk;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSON;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * @Author taoxier
 * @Date 2025/10/16 下午4:30
 * @描述 文档解析工具类：提取文本、分块处理（PDF,DOCX,TXT）
 */
@Component
public class DocumentParser {

    /**
     * 解析文档（自动识别格式，返回分块结果）
     * 
     * @param inputStream 文档输入流（本地临时文件流，无需下载）
     * @param fileType    文件类型（pdf/docx/txt）
     * @return 解析结果（全文+分块+统计信息）
     */
    public DocumentParseResult parse(InputStream inputStream, String fileType) throws Exception {
        DocumentParseResult result = new DocumentParseResult();
        switch (fileType.toLowerCase()) {
            case "pdf":
                parsePdf(inputStream, result);
                break;
            case "docx":
                parseDocx(inputStream, result);
                break;
            case "doc":
                parseDoc(inputStream, result);
                break;
            case "txt":
                parseTxt(inputStream, result);
                break;
            default:
                throw new UnsupportedOperationException("不支持的文件类型：" + fileType);
        }
        // 补充统计信息（调用工具类方法）
        result.setWordCount(DocumentParseUtils.countWords(result.getFullText()));
        result.setCharacterCount(result.getFullText().length());
        return result;
    }

    /**
     * 解析PDF（支持文字/表格分块，公式暂标记为TEXT）
     */
    private void parsePdf(InputStream inputStream, DocumentParseResult result) throws Exception {
        try (PDDocument pdfDoc = PDDocument.load(inputStream)) {
            int pageCount = pdfDoc.getNumberOfPages();
            result.setPageCount(pageCount);

            StringBuilder fullText = new StringBuilder();
            List<ContentChunk> chunks = new ArrayList<>();
            int chunkIndex = 0;

            // 逐页解析
            for (int pageNum = 1; pageNum <= pageCount; pageNum++) {
                // 1. 提取整页文字
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setStartPage(pageNum);
                stripper.setEndPage(pageNum);
                String pageText = stripper.getText(pdfDoc);
                fullText.append(pageText).append("\n");

                // 2. 尝试提取表格（调用工具类方法）
                boolean hasTable = pageText.contains("|") || pageText.contains("\t");
                if (hasTable) {
                    ContentChunk tableChunk = new ContentChunk();
                    tableChunk.setChunkIndex(chunkIndex++);
                    tableChunk.setContentType("TABLE");
                    tableChunk.setContentText("[表格内容]：" + DocumentParseUtils.extractTableLikeContent(pageText));
                    tableChunk.setPageNumber(pageNum);
                    chunks.add(tableChunk);
                }

                // 3. 提取纯文字块
                ContentChunk textChunk = new ContentChunk();
                textChunk.setChunkIndex(chunkIndex++);
                textChunk.setContentType("TEXT");
                textChunk.setContentText(
                        hasTable ? pageText.replace(DocumentParseUtils.extractTableLikeContent(pageText), "")
                                : pageText);
                textChunk.setPageNumber(pageNum);
                chunks.add(textChunk);

                // 4. 公式识别（调用工具类方法）
                String formulaContent = DocumentParseUtils.extractFormulaLikeContent(pageText);
                if (formulaContent != null && !formulaContent.isEmpty()) {
                    ContentChunk formulaChunk = new ContentChunk();
                    formulaChunk.setChunkIndex(chunkIndex++);
                    formulaChunk.setContentType("FORMULA");
                    formulaChunk.setContentText("[公式内容]：" + formulaContent);
                    formulaChunk.setPageNumber(pageNum);
                    chunks.add(formulaChunk);
                }
            }

            result.setFullText(fullText.toString());
            result.setChunks(chunks);
        }
    }

    /**
     * 解析Word（DOCX），精确区分文字/表格
     */
    private void parseDocx(InputStream inputStream, DocumentParseResult result) throws Exception {
        try (XWPFDocument doc = new XWPFDocument(inputStream)) {
            StringBuilder fullText = new StringBuilder();

            // 按文档顺序解析内容（段落和表格交替出现）
            List<IBodyElement> bodyElements = doc.getBodyElements();
            for (IBodyElement element : bodyElements) {
                if (element instanceof XWPFParagraph) {
                    // 解析段落
                    XWPFParagraph paragraph = (XWPFParagraph) element;
                    String paraText = paragraph.getText();
                    if (paraText == null || paraText.trim().isEmpty())
                        continue;

                    // 检查是否是公式
                    String paragraphXml = paragraph.getCTP().xmlText();
                    if (paragraphXml.contains("m:oMath") || paragraphXml.contains("w:equation")) {
                        fullText.append("[DOCX公式]：").append(paraText).append("\n");
                    } else {
                        fullText.append(paraText).append("\n");
                    }
                } else if (element instanceof XWPFTable) {
                    // 解析表格
                    XWPFTable table = (XWPFTable) element;
                    StringBuilder tableText = new StringBuilder();
                    for (XWPFTableRow row : table.getRows()) {
                        for (XWPFTableCell cell : row.getTableCells()) {
                            tableText.append(cell.getText()).append("\t");
                        }
                        tableText.append("\n");
                    }
                    fullText.append(tableText).append("\n");
                }
            }

            result.setFullText(fullText.toString());
            result.setPageCount(doc.getProperties().getExtendedProperties().getUnderlyingProperties().getPages());

            // 使用智能分块
            List<ContentChunk> chunks = performSmartChunking(fullText.toString());
            result.setChunks(chunks);
        }
    }

    /**
     * 解析TXT（仅文字分块）
     */
    private void parseTxt(InputStream inputStream, DocumentParseResult result) throws Exception {
        byte[] bytes = inputStream.readAllBytes();
        String fullText = new String(bytes, "UTF-8");
        result.setFullText(fullText);
        result.setPageCount(1);

        // 使用智能分块
        List<ContentChunk> chunks = performSmartChunking(fullText);
        result.setChunks(chunks);
    }

    /**
     * 解析Word（DOC），使用HWPF库
     */
    private void parseDoc(InputStream inputStream, DocumentParseResult result) throws Exception {
        try (org.apache.poi.hwpf.HWPFDocument doc = new org.apache.poi.hwpf.HWPFDocument(inputStream)) {
            String fullText = doc.getDocumentText();
            result.setFullText(fullText);
            result.setPageCount(1);

            // 使用智能分块
            List<ContentChunk> chunks = performSmartChunking(fullText);
            result.setChunks(chunks);
        }
    }

    /**
     * 智能分块：使用规则预分块
     * 按文档自然结构切分，控制每个基础块长度≤200字符
     */
    private List<ContentChunk> performSmartChunking(String fullText) throws Exception {
        List<ContentChunk> chunks = new ArrayList<>();

        try {
            // 使用规则预分块
            chunks = ruleBasedChunking(fullText);
        } catch (Exception e) {
            // 如果分块失败，使用备用分块方法
            System.err.println("智能分块失败，使用备用分块方法: " + e.getMessage());
            chunks = fallbackChunking(fullText);
        }

        return chunks;
    }

    /**
     * 第一阶段：规则预分块
     * 按文档自然结构切分，控制每个基础块长度≤200字符
     */
    private List<ContentChunk> ruleBasedChunking(String fullText) {
        List<ContentChunk> baseChunks = new ArrayList<>();
        int chunkIndex = 0;
        String currentSection = "正文";

        // 预处理：提取特殊内容
        Map<String, String> specialContentMap = new HashMap<>();
        String processedText = preprocessAndExtractSpecialContent(fullText, specialContentMap);

        // 按段落切分（更灵活的分割方式）
        String[] paragraphs = processedText.split("\n+");
        for (int i = 0; i < paragraphs.length; i++) {
            String paragraph = paragraphs[i].trim();
            if (paragraph.isEmpty())
                continue;

            // 检测是否为标题
            if (isTitle(paragraph) || (i == 0 && paragraph.length() < 50)) {
                // 标题单独作为一个块
                ContentChunk titleChunk = new ContentChunk();
                titleChunk.setChunkIndex(chunkIndex++);
                titleChunk.setContentType("TITLE");
                titleChunk.setContentText(restoreSpecialContent(paragraph, specialContentMap));
                titleChunk.setSectionTitle(paragraph);
                baseChunks.add(titleChunk);

                // 更新 currentSection 为当前标题
                // 所有标题（包括摘要）都应该更新 currentSection
                currentSection = paragraph;
            } else {
                // 处理普通段落
                List<ContentChunk> paraChunks = splitParagraph(paragraph, specialContentMap, chunkIndex,
                        currentSection);
                baseChunks.addAll(paraChunks);
                chunkIndex += paraChunks.size();
            }
        }

        return baseChunks;
    }

    /**
     * 分割段落为基础块，控制长度≤200字符
     */
    private List<ContentChunk> splitParagraph(String paragraph, Map<String, String> specialContentMap, int startIndex,
            String sectionTitle) {
        List<ContentChunk> chunks = new ArrayList<>();

        // 检查是否包含特殊内容标记
        if (paragraph.contains("[TABLE_MARKER_")) {
            // 表格内容
            ContentChunk chunk = new ContentChunk();
            chunk.setChunkIndex(startIndex);
            String content = restoreSpecialContent(paragraph, specialContentMap);
            chunk.setContentType("TABLE");
            chunk.setContentText(content);
            chunk.setSectionTitle(sectionTitle);
            chunks.add(chunk);
        } else if (paragraph.contains("[FORMULA_MARKER_")) {
            // 公式内容
            ContentChunk chunk = new ContentChunk();
            chunk.setChunkIndex(startIndex);
            String content = restoreSpecialContent(paragraph, specialContentMap);
            chunk.setContentType("FORMULA");
            chunk.setContentText(content);
            chunk.setSectionTitle(sectionTitle);
            chunks.add(chunk);
        } else if (paragraph.contains("[IMAGE_MARKER_")) {
            // 图片内容
            ContentChunk chunk = new ContentChunk();
            chunk.setChunkIndex(startIndex);
            String content = restoreSpecialContent(paragraph, specialContentMap);
            chunk.setContentType("IMAGE");
            chunk.setContentText(content);
            chunk.setSectionTitle(sectionTitle);
            chunks.add(chunk);
        } else {
            // 按句子切分，但更智能地处理
            String[] sentences = paragraph.split("[。！？；;]");
            StringBuilder currentChunk = new StringBuilder();
            int chunkIndex = startIndex;

            for (String sentence : sentences) {
                sentence = sentence.trim();
                if (sentence.isEmpty())
                    continue;

                // 检查添加当前句子后是否超过长度限制
                if (currentChunk.length() + sentence.length() + 1 <= 200) {
                    if (currentChunk.length() > 0) {
                        currentChunk.append("。");
                    }
                    currentChunk.append(sentence);
                } else {
                    // 保存当前块
                    if (currentChunk.length() > 0) {
                        ContentChunk chunk = new ContentChunk();
                        chunk.setChunkIndex(chunkIndex++);
                        chunk.setContentType("TEXT");
                        chunk.setContentText(currentChunk.toString() + "。");
                        chunk.setSectionTitle(sectionTitle);
                        chunks.add(chunk);
                    }

                    // 开始新块
                    currentChunk = new StringBuilder(sentence);
                }
            }

            // 保存最后一个块
            if (currentChunk.length() > 0) {
                ContentChunk chunk = new ContentChunk();
                chunk.setChunkIndex(chunkIndex);
                chunk.setContentType("TEXT");
                chunk.setContentText(currentChunk.toString() + "。");
                chunk.setSectionTitle(sectionTitle);
                chunks.add(chunk);
            }
        }

        return chunks;
    }

    /**
     * 第二阶段：AI语义合并
     * 合并语义相关的基础块，控制长度≤300字符
     */
    private List<ContentChunk> semanticMerge(List<ContentChunk> baseChunks) {
        List<ContentChunk> mergedChunks = new ArrayList<>();
        if (baseChunks.isEmpty())
            return mergedChunks;

        ContentChunk currentMerge = baseChunks.get(0);

        for (int i = 1; i < baseChunks.size(); i++) {
            ContentChunk nextChunk = baseChunks.get(i);

            // 检查是否可以合并
            if (canMerge(currentMerge, nextChunk)) {
                // 合并块
                currentMerge = mergeChunks(currentMerge, nextChunk);
            } else {
                // 保存当前合并块
                mergedChunks.add(currentMerge);
                // 开始新的合并
                currentMerge = nextChunk;
            }
        }

        // 保存最后一个合并块
        if (currentMerge != null) {
            mergedChunks.add(currentMerge);
        }

        // 重新编号
        for (int i = 0; i < mergedChunks.size(); i++) {
            mergedChunks.get(i).setChunkIndex(i);
        }

        return mergedChunks;
    }

    /**
     * 判断两个块是否可以合并
     */
    private boolean canMerge(ContentChunk chunk1, ContentChunk chunk2) {
        // 检查长度限制
        int combinedLength = chunk1.getContentText().length() + chunk2.getContentText().length() + 1;
        if (combinedLength > 300) {
            return false;
        }

        // 检查类型：标题不与其他块合并
        if (chunk1.getContentType().equals("TITLE") || chunk2.getContentType().equals("TITLE")) {
            return false;
        }

        // 检查章节：不同章节的块不合并
        if (!chunk1.getSectionTitle().equals(chunk2.getSectionTitle())) {
            return false;
        }

        // 简单的语义相似度判断：检查是否有共同的关键词
        String text1 = chunk1.getContentText().toLowerCase();
        String text2 = chunk2.getContentText().toLowerCase();

        // 提取关键词
        List<String> keywords1 = extractKeywords(text1);
        List<String> keywords2 = extractKeywords(text2);

        // 计算共同关键词数量
        int commonKeywords = 0;
        for (String keyword : keywords1) {
            if (keywords2.contains(keyword)) {
                commonKeywords++;
            }
        }

        // 如果有共同关键词，或者块都很短，允许合并
        return commonKeywords > 0 || (text1.length() < 50 && text2.length() < 50);
    }

    /**
     * 合并两个块
     */
    private ContentChunk mergeChunks(ContentChunk chunk1, ContentChunk chunk2) {
        ContentChunk merged = new ContentChunk();
        merged.setChunkIndex(chunk1.getChunkIndex());
        merged.setContentType(detectContentType(chunk1.getContentText() + chunk2.getContentText()));
        merged.setContentText(chunk1.getContentText() + "\n" + chunk2.getContentText());
        merged.setSectionTitle(chunk1.getSectionTitle());
        return merged;
    }

    /**
     * 提取关键词
     */
    private List<String> extractKeywords(String text) {
        List<String> keywords = new ArrayList<>();
        // 简单的关键词提取：去除停用词，取长度≥2的词
        String[] words = text.split("[^\u4e00-\u9fa5a-zA-Z0-9]+");
        for (String word : words) {
            word = word.trim();
            if (word.length() >= 2 && !isStopWord(word)) {
                keywords.add(word);
            }
        }
        return keywords;
    }

    /**
     * 检查是否为停用词
     */
    private boolean isStopWord(String word) {
        String[] stopWords = { "的", "了", "和", "是", "在", "有", "我", "他", "她", "它", "这", "那", "你", "我", "他", "她", "它", "也",
                "就", "都", "要", "而", "很", "到", "说", "着", "没有", "看", "好", "自己", "这个", "那个" };
        for (String stopWord : stopWords) {
            if (word.equals(stopWord)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 第一阶段：预处理 - 提取特殊内容并替换为唯一标记
     */
    private String preprocessAndExtractSpecialContent(String text, Map<String, String> specialContentMap) {
        String processedText = text;
        final int[] markerIndex = { 0 };

        // 1. 提取公式内容（包含 [DOCX公式] 标记的）
        processedText = extractFormulaContent(processedText, specialContentMap, markerIndex);

        // 2. 提取表格内容（连续多行包含 | 或 \t 的视为一个表格）
        processedText = extractTableContent(processedText, specialContentMap, markerIndex);

        // 3. 提取图片标记
        processedText = extractImageContent(processedText, specialContentMap, markerIndex);

        return processedText;
    }

    /**
     * 提取表格内容（连续多行包含制表符的视为表格）
     */
    private String extractTableContent(String text, Map<String, String> specialContentMap, int[] markerIndex) {
        String[] lines = text.split("\n");
        StringBuilder result = new StringBuilder();
        StringBuilder tableBuffer = new StringBuilder();
        boolean inTable = false;
        int tableStartIndex = -1;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            boolean isTableLine = line.contains("\t");

            // 检测表格开始：如果当前行包含制表符
            if (isTableLine) {
                if (!inTable) {
                    // 开始新表格
                    inTable = true;
                    tableStartIndex = i;
                }
                tableBuffer.append(line).append("\n");
            } else {
                // 当前行不包含制表符
                if (inTable) {
                    // 检查是否应该结束表格
                    // 如果当前空行后面紧跟着非空且非表格的行，则结束表格
                    if (line.trim().isEmpty()) {
                        // 检查下一行
                        if (i + 1 < lines.length) {
                            String nextLine = lines[i + 1];
                            if (!nextLine.contains("\t") && !nextLine.trim().isEmpty()) {
                                // 结束表格
                                String marker = "[TABLE_MARKER_" + markerIndex[0]++ + "]";
                                specialContentMap.put(marker, "TABLE:" + tableBuffer.toString());
                                result.append(marker).append("\n");
                                inTable = false;
                                tableBuffer.setLength(0);
                            }
                        }
                    } else {
                        // 非空行但不是表格行，结束表格
                        String marker = "[TABLE_MARKER_" + markerIndex[0]++ + "]";
                        specialContentMap.put(marker, "TABLE:" + tableBuffer.toString());
                        result.append(marker).append("\n");
                        inTable = false;
                        tableBuffer.setLength(0);
                    }
                }
                result.append(line).append("\n");
            }
        }

        // 处理文件末尾的表格
        if (inTable && tableBuffer.length() > 0) {
            String marker = "[TABLE_MARKER_" + markerIndex[0]++ + "]";
            specialContentMap.put(marker, "TABLE:" + tableBuffer.toString());
            result.append(marker).append("\n");
        }

        return result.toString();
    }

    /**
     * 提取公式内容
     */
    private String extractFormulaContent(String text, Map<String, String> specialContentMap, int[] markerIndex) {
        String[] lines = text.split("\n");
        StringBuilder result = new StringBuilder();
        StringBuilder formulaBuffer = new StringBuilder();
        boolean inFormula = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // 检测公式开始：包含 [DOCX公式] 标记
            if (line.contains("[DOCX公式]")) {
                // 如果之前在公式中，先保存之前的公式
                if (inFormula) {
                    String marker = "[FORMULA_MARKER_" + markerIndex[0]++ + "]";
                    specialContentMap.put(marker, "FORMULA:" + formulaBuffer.toString());
                    result.append(marker).append("\n");
                    formulaBuffer.setLength(0);
                }
                // 开始新公式
                inFormula = true;
                formulaBuffer.append(line).append("\n");
            }
            // 检测公式行：在公式模式下的行
            else if (inFormula) {
                // 检查是否是公式结束：遇到下一个 [DOCX公式] 标记
                if (line.contains("[DOCX公式]")) {
                    // 遇到新的公式标记，保存当前公式并开始新的
                    String marker = "[FORMULA_MARKER_" + markerIndex[0]++ + "]";
                    specialContentMap.put(marker, "FORMULA:" + formulaBuffer.toString());
                    result.append(marker).append("\n");
                    formulaBuffer.setLength(0);

                    // 开始新公式
                    inFormula = true;
                    formulaBuffer.append(line).append("\n");
                } else {
                    // 非空行，继续收集公式内容（包括说明文字）
                    if (!line.trim().isEmpty()) {
                        formulaBuffer.append(line).append("\n");
                    }
                }
            } else {
                result.append(line).append("\n");
            }
        }

        // 处理文件末尾的公式
        if (inFormula && formulaBuffer.length() > 0) {
            String marker = "[FORMULA_MARKER_" + markerIndex[0]++ + "]";
            specialContentMap.put(marker, "FORMULA:" + formulaBuffer.toString());
            result.append(marker).append("\n");
        }

        return result.toString();
    }

    /**
     * 提取图片内容
     */
    private String extractImageContent(String text, Map<String, String> specialContentMap, int[] markerIndex) {
        String[] lines = text.split("\n");
        StringBuilder result = new StringBuilder();

        for (String line : lines) {
            if (line.matches(".*图\\s*\\d+.*") || line.contains("[图片]") ||
                    line.contains("图像") || line.contains("Figure")) {
                String marker = "[IMAGE_MARKER_" + markerIndex[0]++ + "]";
                specialContentMap.put(marker, "IMAGE:" + line);
                result.append(marker).append("\n");
            } else {
                result.append(line).append("\n");
            }
        }

        return result.toString();
    }

    /**
     * 第三阶段：后处理 - 恢复特殊内容
     */
    private String restoreSpecialContent(String chunkText, Map<String, String> specialContentMap) {
        String result = chunkText;
        for (Map.Entry<String, String> entry : specialContentMap.entrySet()) {
            String marker = entry.getKey();
            String content = entry.getValue();
            // 检查该chunk是否包含此标记
            if (result.contains(marker)) {
                // 提取实际内容（去掉类型前缀）
                String actualContent = content.substring(content.indexOf(":") + 1);
                result = result.replace(marker, actualContent);
            }
        }
        return result;
    }

    /**
     * 识别内容类型
     * 
     * @param text 文本内容
     * @return 内容类型：TEXT, TABLE, FORMULA, IMAGE, TITLE, ABSTRACT
     */
    private String detectContentType(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "TEXT";
        }

        String trimmedText = text.trim();

        // 1. 检测表格：包含 | 或 \t 或 "表格" 关键字
        if (trimmedText.contains("|") || trimmedText.contains("\t") ||
                trimmedText.contains("表") && trimmedText.contains("：")) {
            return "TABLE";
        }

        // 2. 检测公式：包含数学符号
        if (containsMathSymbols(trimmedText)) {
            return "FORMULA";
        }

        // 3. 检测图片：包含 "图" + 数字
        if (trimmedText.matches(".*图\\s*\\d+.*") || trimmedText.contains("[图片]") ||
                trimmedText.contains("图像") || trimmedText.contains("Figure")) {
            return "IMAGE";
        }

        // 4. 检测标题：短文本且以章节编号开头
        if (isTitle(trimmedText)) {
            return "TITLE";
        }

        // 5. 检测摘要：包含"摘要"关键字
        if (trimmedText.contains("摘要") || trimmedText.contains("Abstract")) {
            return "ABSTRACT";
        }

        // 默认返回TEXT
        return "TEXT";
    }

    /**
     * 检测是否包含数学符号
     */
    private boolean containsMathSymbols(String text) {
        // 常见的数学符号
        String[] mathSymbols = { "∑", "∫", "√", "^", "÷", "×", "±", "∞", "∂", "∆",
                "α", "β", "γ", "δ", "ε", "θ", "λ", "μ", "π", "σ", "φ", "ω",
                "≤", "≥", "≠", "≈", "≡", "∈", "∉", "⊂", "⊃", "∪", "∩",
                "\\frac", "\\sum", "\\int", "\\sqrt", "\\alpha", "\\beta" };

        for (String symbol : mathSymbols) {
            if (text.contains(symbol)) {
                return true;
            }
        }

        // 检测数学表达式模式：如 x^2, a+b=c, 2x+3y=5
        if (text.matches(".*[a-zA-Z]\\s*[\\+\\-\\*/=]\\s*[a-zA-Z0-9].*") ||
                text.matches(".*\\d+\\s*[\\+\\-\\*/=]\\s*\\d+.*")) {
            return true;
        }

        return false;
    }

    /**
     * 检测是否为标题
     */
    private boolean isTitle(String text) {
        // 标题通常较短（少于50字）
        if (text.length() > 50) {
            return false;
        }

        // 特殊标题：摘要
        if (text.contains("摘要") || text.contains("Abstract")) {
            return true;
        }

        // 以章节编号开头：如 "1.", "1.1", "第一章", "(1)", "（一）"等
        String[] titlePatterns = {
                "^\\d+[\\.、]",
                "^第[一二三四五六七八九十\\d]+[章节部分]",
                "^[（\\(][一二三四五六七八九十\\d]+[）\\)]",
                "^[①②③④⑤⑥⑦⑧⑨⑩]",
                "^[IVXivx]+[\\.、]",
                "^[一二三四五六七八九十]+、",
                "^[一二三四五六七八九十]+\\."
        };

        for (String pattern : titlePatterns) {
            if (text.matches(pattern + ".*")) {
                return true;
            }
        }

        // 检测是否为文档标题（第一行，较短）
        // 这里可以根据实际情况调整

        return false;
    }

    /**
     * 备用分块方法：按姓名+内容分块
     */
    private List<ContentChunk> fallbackChunking(String fullText) {
        List<ContentChunk> chunks = new ArrayList<>();
        String[] lines = fullText.split("\\n|\\r\\n");
        StringBuilder currentChunkText = new StringBuilder();
        boolean isCollectingContent = false;
        int chunkIndex = 0;

        for (String line : lines) {
            if (line == null || line.trim().isEmpty())
                continue;

            // 检测是否为姓名（通常是简短的单行文本）
            boolean isName = line.trim().length() < 10 && !line.contains("，") && !line.contains(",")
                    && !line.contains("。");

            if (isName) {
                // 如果之前有收集的内容，先保存为一个块
                if (currentChunkText.length() > 0) {
                    String chunkText = currentChunkText.toString().trim();
                    ContentChunk chunk = new ContentChunk();
                    chunk.setChunkIndex(chunkIndex++);
                    chunk.setContentType(detectContentType(chunkText));
                    chunk.setContentText(chunkText);
                    chunk.setSectionTitle("正文");
                    chunks.add(chunk);
                    currentChunkText.setLength(0);
                }
                // 开始新的块，添加姓名
                currentChunkText.append(line).append("\n");
                isCollectingContent = true;
            } else if (isCollectingContent) {
                // 继续收集内容
                currentChunkText.append(line).append("\n");
            } else {
                // 不是姓名也不是后续内容，单独作为一个块
                ContentChunk chunk = new ContentChunk();
                chunk.setChunkIndex(chunkIndex++);
                chunk.setContentType(detectContentType(line));
                chunk.setContentText(line);
                chunk.setSectionTitle("正文");
                chunks.add(chunk);
            }
        }

        // 保存最后一个块
        if (currentChunkText.length() > 0) {
            String chunkText = currentChunkText.toString().trim();
            ContentChunk chunk = new ContentChunk();
            chunk.setChunkIndex(chunkIndex++);
            chunk.setContentType(detectContentType(chunkText));
            chunk.setContentText(chunkText);
            chunk.setSectionTitle("正文");
            chunks.add(chunk);
        }

        return chunks;
    }
}