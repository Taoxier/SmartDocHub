package com.taoxier.smartdochub.auth.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信小程序Code登录请求参数
 */
@Schema(description = "微信小程序Code登录请求参数")
@Data
public class WxMiniAppCodeLoginDTO {

    @Schema(description = "微信小程序登录时获取的code", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "code不能为空")
    private String code;

} 