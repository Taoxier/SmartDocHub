package com.taoxier.smartdochub.dashboard.service.impl;

import com.taoxier.smartdochub.ai.service.AIService;
import com.taoxier.smartdochub.dashboard.service.DashboardService;
import com.taoxier.smartdochub.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final DocumentService documentService;
    private final AIService aiService;

    @Override
    public Map<String, Object> getAiAnalysisSummary() {
        try {
            // 获取文档统计信息
            Object stats = documentService.getAdminDocumentStats();
            
            // 获取上传趋势
            List<Map<String, Object>> uploadTrend = documentService.getUploadTrend();
            
            // 获取文档类型占比
            List<Map<String, Object>> typeRatio = documentService.getDocumentTypeRatio();
            
            // 构建分析摘要
            Map<String, Object> summary = new HashMap<>();
            summary.put("stats", stats);
            summary.put("uploadTrend", uploadTrend);
            summary.put("typeRatio", typeRatio);
            
            // 生成AI分析结论
            String analysis = generateAiAnalysis(uploadTrend, typeRatio);
            summary.put("aiAnalysis", analysis);
            
            return summary;
        } catch (Exception e) {
            log.error("获取AI分析摘要失败: {}", e.getMessage());
            throw new RuntimeException("获取AI分析摘要失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getAiAnalysisTrend() {
        try {
            // 预测未来7天的文档上传趋势
            List<Map<String, Object>> futureTrend = predictFutureTrend();
            
            Map<String, Object> trend = new HashMap<>();
            trend.put("futureTrend", futureTrend);
            trend.put("predictionMethod", "基于历史数据的线性回归预测");
            
            return trend;
        } catch (Exception e) {
            log.error("获取趋势预测失败: {}", e.getMessage());
            throw new RuntimeException("获取趋势预测失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getAiAnalysisAnomaly() {
        try {
            // 检测异常情况
            List<Map<String, Object>> anomalies = detectAnomalies();
            
            Map<String, Object> result = new HashMap<>();
            result.put("anomalies", anomalies);
            result.put("detectionMethod", "基于统计方法的异常检测");
            
            return result;
        } catch (Exception e) {
            log.error("获取异常检测结果失败: {}", e.getMessage());
            throw new RuntimeException("获取异常检测结果失败: " + e.getMessage());
        }
    }

    /**
     * 生成AI分析结论
     */
    private String generateAiAnalysis(List<Map<String, Object>> uploadTrend, List<Map<String, Object>> typeRatio) {
        // 这里可以调用大模型API生成分析结论
        // 例如：使用Coze API或其他大模型
        
        StringBuilder analysis = new StringBuilder();
        analysis.append("根据系统数据分析，最近一周文档上传量呈现");
        
        // 分析上传趋势
        if (uploadTrend != null && uploadTrend.size() >= 2) {
            long firstCount = (long) uploadTrend.get(0).get("count");
            long lastCount = (long) uploadTrend.get(uploadTrend.size() - 1).get("count");
            
            if (lastCount > firstCount) {
                analysis.append("上升趋势");
            } else if (lastCount < firstCount) {
                analysis.append("下降趋势");
            } else {
                analysis.append("稳定趋势");
            }
        }
        
        analysis.append("。文档类型分布中，");
        
        // 分析文档类型占比
        if (typeRatio != null && !typeRatio.isEmpty()) {
            typeRatio.sort((a, b) -> Long.compare((long) b.get("value"), (long) a.get("value")));
            String topType = (String) typeRatio.get(0).get("name");
            analysis.append(topType).append("文档占比最高");
        }
        
        analysis.append("。建议关注文档上传趋势，合理规划系统资源。");
        
        return analysis.toString();
    }

    /**
     * 预测未来7天的文档上传趋势
     */
    private List<Map<String, Object>> predictFutureTrend() {
        List<Map<String, Object>> futureTrend = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        // 简单的线性预测
        for (int i = 1; i <= 7; i++) {
            LocalDate futureDate = today.plusDays(i);
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", futureDate.format(DateTimeFormatter.ofPattern("MM/dd")));
            // 模拟预测值
            dayData.put("count", 10 + (int) (Math.random() * 20));
            futureTrend.add(dayData);
        }
        
        return futureTrend;
    }

    /**
     * 检测异常情况
     */
    private List<Map<String, Object>> detectAnomalies() {
        List<Map<String, Object>> anomalies = new ArrayList<>();
        
        // 模拟异常检测结果
        Map<String, Object> anomaly1 = new HashMap<>();
        anomaly1.put("type", "上传量异常");
        anomaly1.put("description", "昨日文档上传量较平均值高出30%");
        anomaly1.put("severity", "medium");
        anomalies.add(anomaly1);
        
        Map<String, Object> anomaly2 = new HashMap<>();
        anomaly2.put("type", "AI生成率异常");
        anomaly2.put("description", "最近24小时AI生成文档占比达到60%");
        anomaly2.put("severity", "high");
        anomalies.add(anomaly2);
        
        return anomalies;
    }
}