package com.taoxier.smartdochub.system.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taoxier.smartdochub.system.model.entity.Config;
import com.taoxier.smartdochub.system.model.form.ConfigForm;
import com.taoxier.smartdochub.system.model.vo.ConfigVO;
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
public class ConfigConverterImpl implements ConfigConverter {

    @Override
    public Page<ConfigVO> toPageVo(Page<Config> page) {
        if ( page == null ) {
            return null;
        }

        Page<ConfigVO> page1 = new Page<ConfigVO>();

        page1.setPages( page.getPages() );
        page1.setRecords( configListToConfigVOList( page.getRecords() ) );
        page1.setTotal( page.getTotal() );
        page1.setSize( page.getSize() );
        page1.setCurrent( page.getCurrent() );

        return page1;
    }

    @Override
    public Config toEntity(ConfigForm configForm) {
        if ( configForm == null ) {
            return null;
        }

        Config config = new Config();

        config.setId( configForm.getId() );
        config.setConfigKey( configForm.getConfigKey() );
        config.setConfigName( configForm.getConfigName() );
        config.setConfigValue( configForm.getConfigValue() );
        config.setRemark( configForm.getRemark() );

        return config;
    }

    @Override
    public ConfigForm toForm(Config entity) {
        if ( entity == null ) {
            return null;
        }

        ConfigForm configForm = new ConfigForm();

        configForm.setConfigKey( entity.getConfigKey() );
        configForm.setConfigName( entity.getConfigName() );
        configForm.setConfigValue( entity.getConfigValue() );
        configForm.setId( entity.getId() );
        configForm.setRemark( entity.getRemark() );

        return configForm;
    }

    @Override
    public ConfigVO toVo(Config entity) {
        if ( entity == null ) {
            return null;
        }

        ConfigVO.ConfigVOBuilder configVO = ConfigVO.builder();

        configVO.configKey( entity.getConfigKey() );
        configVO.configName( entity.getConfigName() );
        configVO.configValue( entity.getConfigValue() );
        configVO.id( entity.getId() );
        configVO.remark( entity.getRemark() );

        return configVO.build();
    }

    protected List<ConfigVO> configListToConfigVOList(List<Config> list) {
        if ( list == null ) {
            return null;
        }

        List<ConfigVO> list1 = new ArrayList<ConfigVO>( list.size() );
        for ( Config config : list ) {
            list1.add( toVo( config ) );
        }

        return list1;
    }
}
