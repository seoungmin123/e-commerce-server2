package kr.hhplus.be.server.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공통 API 응답 포맷
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;  // 요청 성공 여부
    private String message;   // 응답 메시지
    private T data;           // 응답 데이터
    private Integer status;   // HTTP 상태 코드

    public static <T> ApiResponse<T> success(ApiResponseCode code, T data) {
        return new ApiResponse<>(true, code.getMessage(), data, code.getStatus());
    }

    public static <T> ApiResponse<T> fail(ApiResponseCode code, T data) {
        return new ApiResponse<>(false, code.getMessage(), data, code.getStatus());
    }


}
