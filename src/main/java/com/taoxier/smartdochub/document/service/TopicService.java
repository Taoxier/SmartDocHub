package com.taoxier.smartdochub.document.service;

import com.taoxier.smartdochub.document.model.entity.Topic;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 文档主题表 服务类
 * </p>
 *
 * @author taoxier
 * @since 2025-10-16
 */
public interface TopicService extends IService<Topic> {

    /**
     * 获取热门标签
     * 
     * @param limit 返回数量
     * @return 标签列表（包含标签名和出现次数）
     */
    List<Map<String, Object>> getHotTopics(int limit);
}
