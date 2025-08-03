package kr.hhplus.be.server.common.exception;

import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
public class ApiException extends RuntimeException {

    private final ApiErrorCode apiErrorCode;
    private final HttpStatus httpStatus;

    public ApiException(final ApiErrorCode apiErrorCode, final Throwable cause) {
        super(apiErrorCode.getMessage(), cause);
        this.apiErrorCode = apiErrorCode;
        this.httpStatus = apiErrorCode.getHttpStatus();
    }

    public ApiException(final ApiErrorCode apiErrorCode) {
        this(apiErrorCode, null);
    }


}
