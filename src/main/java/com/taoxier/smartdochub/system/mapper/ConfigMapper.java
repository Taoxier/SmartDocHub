package com.taoxier.smartdochub.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taoxier.smartdochub.system.model.entity.Config;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统配置 访问层
 */
@Mapper
public interface ConfigMapper extends BaseMapper<Config> {

}
