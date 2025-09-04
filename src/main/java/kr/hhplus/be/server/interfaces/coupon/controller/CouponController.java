package kr.hhplus.be.server.interfaces.coupon.controller;

import kr.hhplus.be.server.common.response.ApiResponse;
import kr.hhplus.be.server.common.response.ApiResponseCode;
import kr.hhplus.be.server.common.response.ResultResponse;
import kr.hhplus.be.server.interfaces.coupon.controller.swagger.CouponSwaggerDocs;
import kr.hhplus.be.server.domain.coupon.dto.CouponCommand;
import kr.hhplus.be.server.domain.coupon.dto.CouponInfo;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController implements CouponSwaggerDocs {

    private final CouponService couponService;

    /**
     * 사용자 쿠폰 발급 API
     */
    @PostMapping("/{couponId}/issue")
    public ApiResponse<CouponIssueResponse> issueCoupon(@RequestBody User user, @PathVariable Long couponId) {
        CouponInfo couponInfo = couponService.issueCoupon(new CouponCommand.Issue(user, couponId));

        return ApiResponse.success(ApiResponseCode.SUCCESS_OK_200, CouponIssueResponse.from(couponInfo));
    }

    /**
     * 사용자 쿠폰 발급 요청 API
     */
    @PostMapping("/{couponId}/issue-requests")
    public ResponseEntity<ResultResponse>  requestCouponIssue(@RequestBody User user,
                                                              @PathVariable Long couponId) {
        boolean result = couponService.enqueue(new CouponCommand.Issue(user, couponId));
        return ResponseEntity.ok(result ? ResultResponse.success() : ResultResponse.fail("쿠폰 발급 요청에 실패했습니다."));
    }

    /**
     * 사용자 쿠폰 조회 API
     */
    @GetMapping("/my")
    public ApiResponse<List<CouponResponse>> getMyCoupons(@RequestBody User user) {
        List<CouponInfo> couponInfos = couponService.getCoupons(user);

        return ApiResponse.success(ApiResponseCode.SUCCESS_OK_200,
                couponInfos.stream()
                        .map(CouponResponse::from)
                        .toList());
    }
}