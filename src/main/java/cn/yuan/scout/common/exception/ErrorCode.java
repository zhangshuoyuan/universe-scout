package cn.yuan.scout.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS("SUCCESS", "success"),

    SYSTEM_ERROR("GW50000", "system error"),

    PARAM_ERROR("GW40000", "parameter error"),

    UNAUTHORIZED("GW40100", "unauthorized or login expired"),

    FORBIDDEN("GW40300", "forbidden");

    private final String code;

    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
