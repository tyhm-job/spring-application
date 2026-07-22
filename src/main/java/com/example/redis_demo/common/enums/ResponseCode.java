package com.example.redis_demo.common.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ResponseCode {
    // Trường hợp thành công
    SUCCESS(HttpStatus.OK, "200", "Thành công"),

    // Mã lỗi cho các trường hợp thất bại
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER_001", "Email đã tồn tại trong hệ thống"),
    OTP_INVALID_OR_EXPIRED(HttpStatus.BAD_REQUEST, "AUTH_001", "Mã OTP không chính xác hoặc đã hết hạn"),
    OTP_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_002", "Mã OTP đã hết hạn"),
    EMAIL_DOES_NOT_EXIST(HttpStatus.NOT_FOUND, "USER_002", "Email không tồn tại"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_001", "Có lỗi hệ thống xảy ra"),
    MAXIMUM_NUMBER_OF_ATTEMPTS(HttpStatus.TOO_MANY_REQUESTS, "AUTH_003", "Tài khoản tạm khóa do nhập sai quá 5 lần!"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_004", "Email hoặc mật khẩu không chính xác");

    ;

    private final HttpStatus status;
    private final String code;
    private final String message;

    ResponseCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
