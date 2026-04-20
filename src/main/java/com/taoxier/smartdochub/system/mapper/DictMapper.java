package com.taoxier.smartdochub.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taoxier.smartdochub.system.model.entity.Dict;
import com.taoxier.smartdochub.system.model.query.DictPageQuery;
import com.taoxier.smartdochub.system.model.vo.DictPageVO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 字典 访问层
 */
@Mapper
public interface DictMapper extends BaseMapper<Dict> {

    /**
     * 字典分页列表
     *
     * @param page 分页参数
     * @param queryParams 查询参数
     * @return 字典分页列表
     */
    Page<DictPageVO> getDictPage(Page<DictPageVO> page, DictPageQuery queryParams);

}




