package com.taoxier.smartdochub.system.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taoxier.smartdochub.common.model.Option;
import com.taoxier.smartdochub.system.model.entity.Role;
import com.taoxier.smartdochub.system.model.form.RoleForm;
import com.taoxier.smartdochub.system.model.vo.RolePageVO;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-01T00:57:59+0800",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class RoleConverterImpl implements RoleConverter {

    @Override
    public Page<RolePageVO> toPageVo(Page<Role> page) {
        if ( page == null ) {
            return null;
        }

        Page<RolePageVO> page1 = new Page<RolePageVO>();

        page1.setPages( page.getPages() );
        page1.setRecords( roleListToRolePageVOList( page.getRecords() ) );
        page1.setTotal( page.getTotal() );
        page1.setSize( page.getSize() );
        page1.setCurrent( page.getCurrent() );

        return page1;
    }

    @Override
    public Option<Long> toOption(Role role) {
        if ( role == null ) {
            return null;
        }

        Option<Long> option = new Option<Long>();

        option.setValue( role.getId() );
        option.setLabel( role.getName() );

        return option;
    }

    @Override
    public List<Option<Long>> toOptions(List<Role> roles) {
        if ( roles == null ) {
            return null;
        }

        List<Option<Long>> list = new ArrayList<Option<Long>>( roles.size() );
        for ( Role role : roles ) {
            list.add( toOption( role ) );
        }

        return list;
    }

    @Override
    public Role toEntity(RoleForm roleForm) {
        if ( roleForm == null ) {
            return null;
        }

        Role role = new Role();

        role.setId( roleForm.getId() );
        role.setCode( roleForm.getCode() );
        role.setDataScope( roleForm.getDataScope() );
        role.setName( roleForm.getName() );
        role.setSort( roleForm.getSort() );
        role.setStatus( roleForm.getStatus() );

        return role;
    }

    @Override
    public RoleForm toForm(Role entity) {
        if ( entity == null ) {
            return null;
        }

        RoleForm roleForm = new RoleForm();

        roleForm.setCode( entity.getCode() );
        roleForm.setDataScope( entity.getDataScope() );
        roleForm.setId( entity.getId() );
        roleForm.setName( entity.getName() );
        roleForm.setSort( entity.getSort() );
        roleForm.setStatus( entity.getStatus() );

        return roleForm;
    }

    protected RolePageVO roleToRolePageVO(Role role) {
        if ( role == null ) {
            return null;
        }

        RolePageVO rolePageVO = new RolePageVO();

        rolePageVO.setCode( role.getCode() );
        rolePageVO.setCreateTime( role.getCreateTime() );
        rolePageVO.setId( role.getId() );
        rolePageVO.setName( role.getName() );
        rolePageVO.setSort( role.getSort() );
        rolePageVO.setStatus( role.getStatus() );
        rolePageVO.setUpdateTime( role.getUpdateTime() );

        return rolePageVO;
    }

    protected List<RolePageVO> roleListToRolePageVOList(List<Role> list) {
        if ( list == null ) {
            return null;
        }

        List<RolePageVO> list1 = new ArrayList<RolePageVO>( list.size() );
        for ( Role role : list ) {
            list1.add( roleToRolePageVO( role ) );
        }

        return list1;
    }
}
