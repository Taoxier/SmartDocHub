package com.taoxier.smartdochub.config;

import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.captcha.generator.MathGenerator;
import cn.hutool.captcha.generator.RandomGenerator;
import com.taoxier.smartdochub.config.property.CaptchaProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.awt.*;

/**
 * 验证码配置类
 */
@Configuration
@RequiredArgsConstructor
public class CaptchaConfig {

    private final CaptchaProperties captchaProperties;

    /**
     * 验证码字体配置
     */
    @Bean
    public Font captchaFont() {
        CaptchaProperties.FontProperties fontProperties = captchaProperties.getFont();
        int style = Font.PLAIN;
        if (fontProperties.getWeight() == 1) {
            style = Font.BOLD;
        } else if (fontProperties.getWeight() == 2) {
            style = Font.ITALIC;
        }
        return new Font(fontProperties.getName(), style, fontProperties.getSize());
    }

    /**
     * 验证码生成器配置
     */
    @Bean
    public CodeGenerator codeGenerator() {
        CaptchaProperties.CodeProperties codeProperties = captchaProperties.getCode();
        if ("math".equalsIgnoreCase(codeProperties.getType())) {
            return new MathGenerator(codeProperties.getLength());
        } else {
            return new RandomGenerator("0123456789abcdefghijklmnopqrstuvwxyz", codeProperties.getLength());
        }
    }
}
