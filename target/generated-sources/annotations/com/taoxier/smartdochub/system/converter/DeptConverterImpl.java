package com.taoxier.smartdochub.system.converter;

import com.taoxier.smartdochub.system.model.entity.Dept;
import com.taoxier.smartdochub.system.model.form.DeptForm;
import com.taoxier.smartdochub.system.model.vo.DeptVO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-01T00:57:59+0800",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class DeptConverterImpl implements DeptConverter {

    @Override
    public DeptForm toForm(Dept entity) {
        if ( entity == null ) {
            return null;
        }

        DeptForm deptForm = new DeptForm();

        deptForm.setCode( entity.getCode() );
        deptForm.setId( entity.getId() );
        deptForm.setName( entity.getName() );
        deptForm.setParentId( entity.getParentId() );
        deptForm.setSort( entity.getSort() );
        deptForm.setStatus( entity.getStatus() );

        return deptForm;
    }

    @Override
    public DeptVO toVo(Dept entity) {
        if ( entity == null ) {
            return null;
        }

        DeptVO deptVO = new DeptVO();

        deptVO.setCode( entity.getCode() );
        deptVO.setCreateTime( entity.getCreateTime() );
        deptVO.setId( entity.getId() );
        deptVO.setName( entity.getName() );
        deptVO.setParentId( entity.getParentId() );
        deptVO.setSort( entity.getSort() );
        deptVO.setStatus( entity.getStatus() );
        deptVO.setUpdateTime( entity.getUpdateTime() );

        return deptVO;
    }

    @Override
    public Dept toEntity(DeptForm deptForm) {
        if ( deptForm == null ) {
            return null;
        }

        Dept dept = new Dept();

        dept.setId( deptForm.getId() );
        dept.setCode( deptForm.getCode() );
        dept.setName( deptForm.getName() );
        dept.setParentId( deptForm.getParentId() );
        dept.setSort( deptForm.getSort() );
        dept.setStatus( deptForm.getStatus() );

        return dept;
    }
}
