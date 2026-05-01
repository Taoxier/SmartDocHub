package com.taoxier.smartdochub.dashboard.service;

import java.util.Map;

/**
 * 仪表盘服务
 */
public interface DashboardService {

    /**
     * 获取AI分析摘要
     * @return 分析摘要
     */
    Map<String, Object> getAiAnalysisSummary();

    /**
     * 获取趋势预测
     * @return 趋势预测
     */
    Map<String, Object> getAiAnalysisTrend();

    /**
     * 获取异常检测结果
     * @return 异常检测结果
     */
    Map<String, Object> getAiAnalysisAnomaly();
}