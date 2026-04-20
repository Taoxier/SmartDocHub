package com.taoxier.smartdochub.document.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.taoxier.smartdochub.document.mapper.DocumentMapper;
import com.taoxier.smartdochub.document.model.entity.Document;
import com.taoxier.smartdochub.document.service.KgService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 知识图谱构建定时任务
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class KnowledgeGraphTask {

    private final DocumentMapper documentMapper;
    private final KgService kgService;

    /**
     * 每天凌晨2点执行知识图谱构建
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void buildKnowledgeGraph() {
        log.info("开始执行知识图谱构建定时任务");

        // 获取所有已完成处理的文档
        List<Document> documents = documentMapper.selectList(
                new LambdaQueryWrapper<Document>()
                        .eq(Document::getProcessStatus, "COMPLETED")
                        .last("LIMIT 100")
        );

        log.info("找到 {} 个文档需要构建知识图谱", documents.size());

        // 批量构建知识图谱
        List<Long> documentIds = documents.stream()
                .map(Document::getId)
                .toList();

        if (!documentIds.isEmpty()) {
            kgService.batchBuildKnowledgeGraph(documentIds);
        }

        log.info("知识图谱构建定时任务执行完成");
    }
}
