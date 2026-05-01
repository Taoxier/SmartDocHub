package com.taoxier.smartdochub.document.util;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ciModel.auditing.DocumentAuditingJobsDetail;
import com.qcloud.cos.model.ciModel.auditing.DocumentAuditingRequest;
import com.qcloud.cos.model.ciModel.auditing.DocumentAuditingResponse;
import com.qcloud.cos.model.ciModel.auditing.DocumentInputObject;
import com.qcloud.cos.model.ciModel.auditing.DocumentResultInfo;
import com.qcloud.cos.model.ciModel.auditing.Conf;
import com.qcloud.cos.region.Region;
import com.taoxier.smartdochub.config.properties.TencentCloudProperties;
import lombok.extern.slf4j.Slf4j;

/**
 * 腾讯云文档审核工具类
 */
@Slf4j
public class DocumentAuditHelper {

    private static TencentCloudProperties tencentCloudProperties;

    public static void setTencentCloudProperties(TencentCloudProperties properties) {
        tencentCloudProperties = properties;
    }

    public static COSClient createCOSClient() {
        if (tencentCloudProperties == null) {
            throw new RuntimeException("腾讯云配置未初始化");
        }

        COSCredentials credentials = new BasicCOSCredentials(
                tencentCloudProperties.getSecretId(),
                tencentCloudProperties.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(tencentCloudProperties.getRegion()));
        return new COSClient(credentials, clientConfig);
    }

    /**
     * 提交文档审核任务
     * 
     * @param cosClient  COS客户端
     * @param bucketName 存储桶名称
     * @param cosKey     COS文件路径
     * @return 审核任务ID
     */
    public static String submitAudit(COSClient cosClient, String bucketName, String cosKey) {
        DocumentAuditingRequest request = new DocumentAuditingRequest();
        request.setBucketName(bucketName);

        DocumentInputObject input = new DocumentInputObject();
        input.setObject(cosKey);
        request.setInput(input);

        Conf conf = new Conf();
        conf.setDetectType("Porn,Ads,Illegal,Abuse");
        request.setConf(conf);

        DocumentAuditingResponse response = cosClient.createAuditingDocumentJobs(request);
        String jobId = response.getJobsDetail().getJobId();

        log.info("提交文档审核任务成功，JobId: {}, COS Key: {}", jobId, cosKey);
        return jobId;
    }

    /**
     * 轮询获取审核结果
     * 
     * @param cosClient  COS客户端
     * @param bucketName 存储桶名称
     * @param jobId      审核任务ID
     * @return 审核结果
     */
    public static AuditResult pollResult(COSClient cosClient, String bucketName, String jobId) {
        int maxRetry = 40;
        int interval = 3000;

        for (int i = 0; i < maxRetry; i++) {
            DocumentAuditingRequest queryReq = new DocumentAuditingRequest();
            queryReq.setBucketName(bucketName);
            queryReq.setJobId(jobId);

            DocumentAuditingResponse response = cosClient.describeAuditingDocumentJob(queryReq);
            DocumentAuditingJobsDetail detail = response.getJobsDetail();
            String state = detail.getState();

            log.info("第{}次查询审核状态: {}, JobId: {}", i + 1, state, jobId);

            switch (state) {
                case "Success":
                    return parseResult(detail);
                case "Failed":
                    log.error("审核失败: {}, JobId: {}", detail.getMessage(), jobId);
                    return new AuditResult(false, "审核失败: " + detail.getMessage());
                default:
                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return new AuditResult(false, "轮询被中断");
                    }
            }
        }

        log.warn("审核轮询超时，JobId: {}", jobId);
        return new AuditResult(false, "审核超时，请稍后手动查询");
    }

    /**
     * 解析审核结果
     */
    private static AuditResult parseResult(DocumentAuditingJobsDetail detail) {
        String suggestion = detail.getSuggestion();
        String label = detail.getLabel();
        boolean hasViolation = false;
        StringBuilder violationInfo = new StringBuilder();

        if ("1".equals(suggestion) || "2".equals(suggestion)) {
            hasViolation = true;
            violationInfo.append("整体建议: ").append("1".equals(suggestion) ? "违规" : "疑似");
            if (label != null) {
                violationInfo.append(", 违规类型: ").append(label);
            }
        }

        try {
            DocumentResultInfo labels = detail.getLabels();
            if (labels != null) {
                if (labels.getPornInfo() != null && "1".equals(labels.getPornInfo().getHitFlag())) {
                    violationInfo.append("; 色情内容: ").append(labels.getPornInfo().getScore()).append("分");
                }
                if (labels.getAdsInfo() != null && "1".equals(labels.getAdsInfo().getHitFlag())) {
                    violationInfo.append("; 广告内容: ").append(labels.getAdsInfo().getScore()).append("分");
                }
                if (labels.getPoliticsInfo() != null && "1".equals(labels.getPoliticsInfo().getHitFlag())) {
                    violationInfo.append("; 政治内容: ").append(labels.getPoliticsInfo().getScore()).append("分");
                }
                if (labels.getTerroristInfo() != null && "1".equals(labels.getTerroristInfo().getHitFlag())) {
                    violationInfo.append("; 暴恐内容: ").append(labels.getTerroristInfo().getScore()).append("分");
                }
            }
        } catch (Exception e) {
            log.warn("获取详细审核分类信息失败: {}", e.getMessage());
        }

        if (hasViolation) {
            return new AuditResult(false, violationInfo.toString());
        } else {
            return new AuditResult(true, "审核通过");
        }
    }

    /**
     * 审核结果封装
     */
    public static class AuditResult {
        private boolean passed;
        private String message;

        public AuditResult(boolean passed, String message) {
            this.passed = passed;
            this.message = message;
        }

        public boolean isPassed() {
            return passed;
        }

        public void setPassed(boolean passed) {
            this.passed = passed;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}