package com.taoxier.smartdochub.document.mapper;

import com.taoxier.smartdochub.document.model.entity.KgNode;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 知识图谱节点表 Mapper 接口
 * </p>
 *
 * @author taoxier
 * @since 2025-10-16
 */
@Mapper
public interface KgNodeMapper extends BaseMapper<KgNode> {

    @Select("SELECT n.* FROM kg_node n INNER JOIN (SELECT node_name, COUNT(*) as cnt FROM kg_node GROUP BY node_name ORDER BY cnt DESC LIMIT #{limit}) t ON n.node_name = t.node_name")
    List<KgNode> selectTopNodes(int limit);
}