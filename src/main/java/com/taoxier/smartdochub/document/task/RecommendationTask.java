package com.taoxier.smartdochub.document.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

@Slf4j
@Component
public class RecommendationTask {

    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void generateRecommendations() {
        log.info("开始执行文档推荐任务");
        
        String pythonScriptPath = "src/main/resources/scripts/recommend.py";
        
        try {
            // 构建Python命令
            ProcessBuilder pb = new ProcessBuilder("python", pythonScriptPath);
            pb.directory(new java.io.File(System.getProperty("user.dir")));
            pb.redirectErrorStream(true);
            
            // 执行Python脚本
            Process process = pb.start();
            
            // 读取输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("推荐脚本输出: {}", line);
            }
            
            // 等待脚本执行完成
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.info("文档推荐任务执行成功");
            } else {
                log.error("文档推荐任务执行失败，退出码: {}", exitCode);
            }
            
        } catch (IOException | InterruptedException e) {
            log.error("执行推荐脚本时发生错误", e);
        }
    }
}
