package com.taoxier.smartdochub.document.util.parse;

import cn.hutool.core.util.StrUtil;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

/**
 * 文档解析辅助工具类（存放通用方法，无状态）
 */
public class DocumentParseUtils {

    /**
     * 简单提取类表格内容（PDF用）
     */
    public static String extractTableLikeContent(String text) {
        StringBuilder tableText = new StringBuilder();
        for (String line : text.split("\n")) {
            if (line.contains("|") || line.contains("\t") && line.split("\t").length >= 3) {
                tableText.append(line).append("\n");
            }
        }
        return tableText.toString();
    }

    /**
     * 简单提取类公式内容（包含特殊符号）
     */
    public static String extractFormulaLikeContent(String text) {
        StringBuilder formulaText = new StringBuilder();
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.matches(".*[\\=\\+\\-\\*\\/\\(\\)\\[\\]\\{\\}\\,\\.\\<\\>\\≥\\≤\\≠\\√\\π\\∑\\∫].*")
                    && line.length() < 50) { // 短且含特殊符号，暂认为是公式
                formulaText.append(line).append("\n");
            }
        }
        return formulaText.toString();
    }

    /**
     * 获取Word段落样式（判断是否为章节标题）
     */
    public static String getParagraphStyle(XWPFParagraph paragraph) {
        String styleId = paragraph.getStyleID();
        if (styleId == null) return "正文";
        // 匹配Word内置标题样式（如Heading1=一级标题）
        if (styleId.startsWith("Heading")) {
            return styleId.replace("Heading", "第") + "级标题";
        }
        return "正文";
    }

    /**
     * 统计字数（按空格分割，简化版）
     */
    public static int countWords(String text) {
        if (StrUtil.isBlank(text)) return 0;
        return text.trim().split("\\s+").length;
    }
}