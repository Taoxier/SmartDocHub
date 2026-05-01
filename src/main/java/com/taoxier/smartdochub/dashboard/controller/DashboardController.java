package com.taoxier.smartdochub.dashboard.controller;

import com.taoxier.smartdochub.common.result.Result;
import com.taoxier.smartdochub.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 仪表盘控制器
 */
@Tag(name = "管理仪表盘")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/ai-analysis/summary")
    @Operation(summary = "获取AI分析摘要")
    public Result<Map<String, Object>> getAiAnalysisSummary() {
        try {
            Map<String, Object> summary = dashboardService.getAiAnalysisSummary();
            return Result.success(summary);
        } catch (Exception e) {
            log.error("获取AI分析摘要失败: {}", e.getMessage());
            return Result.failed("获取AI分析摘要失败: " + e.getMessage());
        }
    }

    @GetMapping("/ai-analysis/trend")
    @Operation(summary = "获取趋势预测")
    public Result<Map<String, Object>> getAiAnalysisTrend() {
        try {
            Map<String, Object> trend = dashboardService.getAiAnalysisTrend();
            return Result.success(trend);
        } catch (Exception e) {
            log.error("获取趋势预测失败: {}", e.getMessage());
            return Result.failed("获取趋势预测失败: " + e.getMessage());
        }
    }

    @GetMapping("/ai-analysis/anomaly")
    @Operation(summary = "获取异常检测结果")
    public Result<Map<String, Object>> getAiAnalysisAnomaly() {
        try {
            Map<String, Object> anomaly = dashboardService.getAiAnalysisAnomaly();
            return Result.success(anomaly);
        } catch (Exception e) {
            log.error("获取异常检测结果失败: {}", e.getMessage());
            return Result.failed("获取异常检测结果失败: " + e.getMessage());
        }
    }
}