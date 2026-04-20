package com.taoxier.smartdochub.system.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taoxier.smartdochub.system.model.entity.Config;
import com.taoxier.smartdochub.system.model.form.ConfigForm;
import com.taoxier.smartdochub.system.model.vo.ConfigVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 系统配置转换器
 */
@Mapper(componentModel = "spring")
public interface ConfigConverter {

    ConfigConverter INSTANCE = Mappers.getMapper(ConfigConverter.class);

    /**
     * 转换为分页视图对象
     */
    Page<ConfigVO> toPageVo(Page<Config> page);

    /**
     * 转换为实体
     */
    Config toEntity(ConfigForm configForm);

    /**
     * 转换为表单
     */
    ConfigForm toForm(Config entity);

    /**
     * 转换为视图对象
     */
    ConfigVO toVo(Config entity);

}