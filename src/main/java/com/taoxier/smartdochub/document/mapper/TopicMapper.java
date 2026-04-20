package com.taoxier.smartdochub.document.mapper;

import com.taoxier.smartdochub.document.model.entity.Topic;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 文档主题表 Mapper 接口
 * </p>
 *
 * @author taoxier
 * @since 2025-10-16
 */
@Mapper
public interface TopicMapper extends BaseMapper<Topic> {

    @Select("SELECT topic_value, COUNT(*) as cnt FROM doc_topic WHERE topic_type = 'KEYWORD' GROUP BY topic_value ORDER BY cnt DESC LIMIT #{limit}")
    List<Map<String, Object>> selectHotTopics(int limit);
}