package com.taoxier.smartdochub.system.converter;

import com.taoxier.smartdochub.system.model.entity.Menu;
import com.taoxier.smartdochub.system.model.form.MenuForm;
import com.taoxier.smartdochub.system.model.vo.MenuVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 菜单对象转换器
 */
@Mapper(componentModel = "spring")
public interface MenuConverter {

    MenuVO toVo(Menu entity);

    @Mapping(target = "params", ignore = true)
    MenuForm toForm(Menu entity);

    @Mapping(target = "params", ignore = true)
    Menu toEntity(MenuForm menuForm);

}