package com.taoxier.smartdochub.system.converter;

import com.taoxier.smartdochub.system.model.entity.Dept;
import com.taoxier.smartdochub.system.model.form.DeptForm;
import com.taoxier.smartdochub.system.model.vo.DeptVO;
import org.mapstruct.Mapper;

/**
 * 部门对象转换器
 */
@Mapper(componentModel = "spring")
public interface DeptConverter {

    DeptForm toForm(Dept entity);
    
    DeptVO toVo(Dept entity);

    Dept toEntity(DeptForm deptForm);

}