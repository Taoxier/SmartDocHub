package com.taoxier.smartdochub.document.util.parse;

import com.taoxier.smartdochub.document.model.entity.ContentChunk;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Component;

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
            List<ContentChunk> chunks = new ArrayList<>();
            int chunkIndex = 0;

            // 1. 解析段落（文字）
            for (XWPFParagraph paragraph : doc.getParagraphs()) {
                String paraText = paragraph.getText();
                if (paraText == null || paraText.isEmpty())
                    continue;

                fullText.append(paraText).append("\n");
                ContentChunk textChunk = new ContentChunk();
                textChunk.setChunkIndex(chunkIndex++);
                textChunk.setContentType("TEXT");
                textChunk.setContentText(paraText);
                textChunk.setSectionTitle(DocumentParseUtils.getParagraphStyle(paragraph)); // 调用工具类
                chunks.add(textChunk);
            }

            // 2. 解析表格
            for (org.apache.poi.xwpf.usermodel.XWPFTable table : doc.getTables()) {
                StringBuilder tableText = new StringBuilder("[表格]：\n");
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        tableText.append(cell.getText()).append("\t");
                    }
                    tableText.append("\n");
                }

                fullText.append(tableText).append("\n");
                ContentChunk tableChunk = new ContentChunk();
                tableChunk.setChunkIndex(chunkIndex++);
                tableChunk.setContentType("TABLE");
                tableChunk.setContentText(tableText.toString());
                tableChunk.setOriginalContent(table.toString());
                chunks.add(tableChunk);
            }

            // 3. 解析公式（兼容POI 5.x版本）
            for (XWPFParagraph paragraph : doc.getParagraphs()) {
                // 获取段落的原始XML内容，判断是否包含公式标签
                String paragraphXml = paragraph.getCTP().xmlText();
                // DOCX公式的XML标签通常是 <m:oMath> 或 <w:equation>
                if (paragraphXml.contains("m:oMath") || paragraphXml.contains("w:equation")) {
                    ContentChunk formulaChunk = new ContentChunk();
                    formulaChunk.setChunkIndex(chunkIndex++);
                    formulaChunk.setContentType("FORMULA");
                    formulaChunk.setContentText("[DOCX公式]：" + paragraph.getText());
                    chunks.add(formulaChunk);
                }
            }

            result.setFullText(fullText.toString());
            result.setChunks(chunks);
            result.setPageCount(doc.getProperties().getExtendedProperties().getUnderlyingProperties().getPages());
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

        // 按换行符分块（每5行一个块）
        List<ContentChunk> chunks = new ArrayList<>();
        String[] lines = fullText.split("\n");
        StringBuilder chunkText = new StringBuilder();
        int chunkIndex = 0;

        for (int i = 0; i < lines.length; i++) {
            chunkText.append(lines[i]).append("\n");
            // 每5行或最后一行时保存块
            if ((i + 1) % 5 == 0 || i == lines.length - 1) {
                ContentChunk chunk = new ContentChunk();
                chunk.setChunkIndex(chunkIndex++);
                chunk.setContentType("TEXT");
                chunk.setContentText(chunkText.toString());
                chunks.add(chunk);
                chunkText.setLength(0); // 清空
            }
        }

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

            // 按段落分块
            List<ContentChunk> chunks = new ArrayList<>();
            String[] paragraphs = fullText.split("\r\n");
            int chunkIndex = 0;

            for (String paragraph : paragraphs) {
                if (paragraph == null || paragraph.trim().isEmpty())
                    continue;

                ContentChunk chunk = new ContentChunk();
                chunk.setChunkIndex(chunkIndex++);
                chunk.setContentType("TEXT");
                chunk.setContentText(paragraph);
                chunks.add(chunk);
            }

            result.setChunks(chunks);
        }
    }
}