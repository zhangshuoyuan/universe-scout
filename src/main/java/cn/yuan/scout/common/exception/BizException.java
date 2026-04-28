package cn.yuan.scout.common.exception;

import lombok.Getter;

/**
 * 业务异常。
 */
@Getter
public class BizException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * 根据错误码构造业务异常。
     *
     * @param errorCode 错误码
     */
    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 根据错误码和自定义消息构造业务异常。
     *
     * @param errorCode 错误码
     * @param message 自定义异常消息
     */
    public BizException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 根据错误码和原始异常构造业务异常。
     *
     * @param errorCode 错误码
     * @param cause 原始异常
     */
    public BizException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    /**
     * 根据错误码、自定义消息和原始异常构造业务异常。
     *
     * @param errorCode 错误码
     * @param message 自定义异常消息
     * @param cause 原始异常
     */
    public BizException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
