package kr.hhplus.be.server.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 공통 API 응답 코드,메시지
 */
@Getter
@AllArgsConstructor
public enum ApiResponseCode {
    //테스트
    OK(200,"테스트"),

    //성공
    SUCCESS_OK_200(200, "성공"),
    SUCCESS_CREATED_201(201, "생성 완료"),

    // 실패
    //공통
    FAIL_BAD_REQUEST_400(400, "잘못된 요청"),
    FAIL_MISSING_PARAMETER_400(400, "요청 파라미터 누락"),
    FAIL_INVALID_INPUT_400(400, "입력값이 올바르지 않음"),

    //쿠폰
    FAIL_COUPON_EXPIRED_400(400, "만료된 쿠폰입니다."),
    FAIL_COUPON_ALREADY_USED_400(400, "이미 사용된 쿠폰입니다."),
    FAIL_COUPON_NOT_FOUND_400(400, "쿠폰이 존재하지 않습니다."),
    FAIL_COUPON_INVALID_POLICY_400(400, "유효하지 않은 쿠폰 정책입니다."),
    FAIL_COUPON_INVALID_REQUEST(400, "유효하지 않은 요청입니다."),

    //포인트
    FAIL_POINT_INSUFFICIENT_400(400, "포인트가 부족합니다."),
    FAIL_INVALID_PAYMENT_AMOUNT_400(400, "결제 금액이 올바르지 않습니다."),

    //재고
    FAIL_PRODUCT_NOT_FOUND_400(400, "상품이 존재하지 않습니다."),
    FAIL_PRODUCT_STOCK_SHORTAGE_400(400, "상품 재고가 부족합니다."),
    FAIL_INVALID_PRODUCT_QUANTITY_400(400, "상품 수량은 1 이상이어야 합니다."),

    //주문
    FAIL_ORDER_ALREADY_COMPLETED_400(400, "이미 완료된 주문입니다."),
    FAIL_ORDER_STATUS_INVALID_400(400, "현재 상태에서 수행할 수 없는 주문입니다."),
    FAIL_ORDER_INVALID_STATUS_TRANSITION_400(400,"결제되지 않은 주문을 완료 처리 요청"),

    FAIL_UNAUTHORIZED_401(401, "인증 실패"),
    FAIL_FORBIDDEN_403(403, "권한 없음"),
    FAIL_NOT_FOUND_404(404, "데이터 없음"),

    //공통
    FAIL_CONFLICT_409(409, "데이터 일단 충돌"),

    //주문
    FAIL_ORDER_CONFLICT_409(409, "동시에 주문 요청해서 중복 처리됨"),

    FAIL_INTERNAL_ERROR_500(500, "서버 오류");



    private final int status;
    private final String message;
}