package kr.hhplus.be.server.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 공통 API 예외 응답 포맷
 */
@Getter
@AllArgsConstructor
public class ApiErrorResponse {

    private final int status;
    private final String message;

    // 코드 기반 기본 생성
    public static ApiErrorResponse fromErrorCode(ApiResponseCode code) {
        return new ApiErrorResponse(code.getStatus(), code.getMessage());
    }

    // 커스텀 메시지 포함 생성
    public static ApiErrorResponse fromErrorCodeWithMessage(ApiResponseCode code, String overrideMessage) {
        return new ApiErrorResponse(code.getStatus(), overrideMessage);
    }

    // http 상태코드
    public static ApiErrorResponse fromErrorCodeWithMessage(HttpStatus httpStatus, String overrideMessage) {
        return new ApiErrorResponse(httpStatus.value(), overrideMessage);
    }
}