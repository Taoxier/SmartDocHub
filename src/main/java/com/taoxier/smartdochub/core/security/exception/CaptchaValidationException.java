package com.taoxier.smartdochub.core.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * 验证码校验异常
 */
public class CaptchaValidationException extends AuthenticationException {
    public CaptchaValidationException(String msg) {
        super(msg);
    }
}