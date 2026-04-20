package com.taoxier.smartdochub.config;

import com.taoxier.smartdochub.system.converter.ConfigConverter;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 转换器配置类
 */
@Configuration
public class ConverterConfig {

    /**
     * 配置 ConfigConverter Bean
     */
    @Bean
    public ConfigConverter configConverter() {
        return Mappers.getMapper(ConfigConverter.class);
    }

}