package com.taoxier.smartdochub.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.taoxier.smartdochub.document.model.entity.Topic;
import com.taoxier.smartdochub.document.mapper.TopicMapper;
import com.taoxier.smartdochub.document.service.TopicService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 文档主题表 服务实现类
 * </p>
 *
 * @author taoxier
 * @since 2025-10-16
 */
@Service
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService {

    @Override
    public List<Map<String, Object>> getHotTopics(int limit) {
        return baseMapper.selectHotTopics(limit);
    }
}
