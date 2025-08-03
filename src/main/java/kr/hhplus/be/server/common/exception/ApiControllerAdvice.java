package kr.hhplus.be.server.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import kr.hhplus.be.server.common.response.ApiErrorResponse;
import kr.hhplus.be.server.common.response.ApiResponseCode;
import kr.hhplus.be.server.coupon.exception.CouponException;
import kr.hhplus.be.server.coupon.exception.CouponPolicyException;
import kr.hhplus.be.server.order.exception.OrderException;
import kr.hhplus.be.server.product.exception.ProductException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * 공통 API 예외처리 포맷
 */
@RestControllerAdvice
public class ApiControllerAdvice extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiControllerAdvice.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiCustomException(
            ApiException ex,
            HttpServletRequest request
    ) {
        log.error("Response: {} {} body={}",
                request.getMethod(),
                request.getRequestURI(),
                ApiErrorResponse.fromErrorCodeWithMessage(
                        ex.getApiErrorCode().getHttpStatus(), ex.getMessage())
        );

        return ResponseEntity
                .status(ex.getApiErrorCode().getHttpStatus())
                .body(ApiErrorResponse.fromErrorCodeWithMessage( ex.getApiErrorCode().getHttpStatus(), ex.getMessage()));
    }

    //404
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NotFoundException ex) {
        log.warn("[NotFound] {}", ex.getMessage());
        ApiResponseCode code = ApiResponseCode.FAIL_NOT_FOUND_404;
        return ResponseEntity
                .status(code.getStatus())
                .body(ApiErrorResponse.fromErrorCodeWithMessage(code, ex.getMessage()));
    }

    //주문관련
    @ExceptionHandler(OrderException.class)
    public ResponseEntity<ApiErrorResponse> handleOrderException(OrderException ex) {
        log.warn("[주문 예외] {} - {}", ex.getErrorCode(), ex.getMessage());
        return ResponseEntity
                .status(ex.getErrorCode().getStatus())
                .body(ApiErrorResponse.fromErrorCodeWithMessage(ex.getErrorCode(), ex.getMessage()));
    }

    //쿠폰 관련
    @ExceptionHandler(CouponException.class)
    public ResponseEntity<ApiErrorResponse> handleCouponException(CouponException ex) {
        log.warn("[쿠폰 예외] {} - {}", ex.getErrorCode(), ex.getMessage());
        return ResponseEntity
                .status(ex.getErrorCode().getStatus())
                .body(ApiErrorResponse.fromErrorCodeWithMessage(ex.getErrorCode(), ex.getMessage()));
    }

    //쿠폰 정책관련
    @ExceptionHandler(CouponPolicyException.class)
    public ResponseEntity<ApiErrorResponse> handleCouponPolicyException(CouponPolicyException ex) {
        log.warn("[쿠폰 예외] {} - {}", ex.getErrorCode(), ex.getMessage());

        return ResponseEntity
                .status(ex.getErrorCode().getStatus())
                .body(ApiErrorResponse.fromErrorCodeWithMessage(ex.getErrorCode(), ex.getMessage()));
    }

    //재고관련 오류
    @ExceptionHandler(ProductException.class)
    public ResponseEntity<ApiErrorResponse> handleProductException(ProductException ex) {
        log.warn("[재고 예외] {} - {}", ex.getErrorCode(), ex.getMessage());
        ApiResponseCode code = ApiResponseCode.FAIL_BAD_REQUEST_400;
        return ResponseEntity
                .status(code.getStatus())
                .body(ApiErrorResponse.fromErrorCodeWithMessage(code, ex.getMessage()));
    }

    //검증 오류 등 단순 잘못된 입력
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        log.warn("[BadRequest] {}", ex.getMessage());

        ApiResponseCode code = ApiResponseCode.FAIL_BAD_REQUEST_400;

        return ResponseEntity
                .status(code.getStatus())
                .body(ApiErrorResponse.fromErrorCodeWithMessage(code, ex.getMessage()));
    }

    //기타 서버 에러처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnknown(Exception ex,
                                                          HttpServletRequest request) {
        log.error("[Unexpected Error]" + ex); // Stack trace 포함

        ApiResponseCode code = ApiResponseCode.FAIL_INTERNAL_ERROR_500;

        return ResponseEntity
                .status(code.getStatus())
                .body(ApiErrorResponse.fromErrorCode(code));
    }


}