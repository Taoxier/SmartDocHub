package com.taoxier.smartdochub.system.converter;

import com.taoxier.smartdochub.system.model.entity.Menu;
import com.taoxier.smartdochub.system.model.form.MenuForm;
import com.taoxier.smartdochub.system.model.vo.MenuVO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-01T00:57:59+0800",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class MenuConverterImpl implements MenuConverter {

    @Override
    public MenuVO toVo(Menu entity) {
        if ( entity == null ) {
            return null;
        }

        MenuVO menuVO = new MenuVO();

        menuVO.setComponent( entity.getComponent() );
        menuVO.setIcon( entity.getIcon() );
        menuVO.setId( entity.getId() );
        menuVO.setName( entity.getName() );
        menuVO.setParentId( entity.getParentId() );
        menuVO.setPerm( entity.getPerm() );
        menuVO.setRedirect( entity.getRedirect() );
        menuVO.setRouteName( entity.getRouteName() );
        menuVO.setRoutePath( entity.getRoutePath() );
        menuVO.setSort( entity.getSort() );
        menuVO.setType( entity.getType() );
        menuVO.setVisible( entity.getVisible() );

        return menuVO;
    }

    @Override
    public MenuForm toForm(Menu entity) {
        if ( entity == null ) {
            return null;
        }

        MenuForm menuForm = new MenuForm();

        menuForm.setAlwaysShow( entity.getAlwaysShow() );
        menuForm.setComponent( entity.getComponent() );
        menuForm.setIcon( entity.getIcon() );
        menuForm.setId( entity.getId() );
        menuForm.setKeepAlive( entity.getKeepAlive() );
        menuForm.setName( entity.getName() );
        menuForm.setParentId( entity.getParentId() );
        menuForm.setPerm( entity.getPerm() );
        menuForm.setRedirect( entity.getRedirect() );
        menuForm.setRouteName( entity.getRouteName() );
        menuForm.setRoutePath( entity.getRoutePath() );
        menuForm.setSort( entity.getSort() );
        menuForm.setType( entity.getType() );
        menuForm.setVisible( entity.getVisible() );

        return menuForm;
    }

    @Override
    public Menu toEntity(MenuForm menuForm) {
        if ( menuForm == null ) {
            return null;
        }

        Menu menu = new Menu();

        menu.setAlwaysShow( menuForm.getAlwaysShow() );
        menu.setComponent( menuForm.getComponent() );
        menu.setIcon( menuForm.getIcon() );
        menu.setId( menuForm.getId() );
        menu.setKeepAlive( menuForm.getKeepAlive() );
        menu.setName( menuForm.getName() );
        menu.setParentId( menuForm.getParentId() );
        menu.setPerm( menuForm.getPerm() );
        menu.setRedirect( menuForm.getRedirect() );
        menu.setRouteName( menuForm.getRouteName() );
        menu.setRoutePath( menuForm.getRoutePath() );
        menu.setSort( menuForm.getSort() );
        menu.setType( menuForm.getType() );
        menu.setVisible( menuForm.getVisible() );

        return menu;
    }
}
