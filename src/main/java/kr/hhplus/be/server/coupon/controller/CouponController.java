package kr.hhplus.be.server.coupon.controller;

import kr.hhplus.be.server.common.response.ApiResponse;
import kr.hhplus.be.server.common.response.ApiResponseCode;
import kr.hhplus.be.server.coupon.controller.swagger.CouponSwaggerDocs;
import kr.hhplus.be.server.coupon.dto.CouponCommand;
import kr.hhplus.be.server.coupon.dto.CouponInfo;
import kr.hhplus.be.server.coupon.service.CouponService;
import kr.hhplus.be.server.user.domain.User;
import lombok.RequiredArgsConstructor;
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