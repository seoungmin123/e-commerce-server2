package kr.hhplus.be.server.point.controller;


import kr.hhplus.be.server.common.response.ApiResponse;
import kr.hhplus.be.server.common.response.ApiResponseCode;
import kr.hhplus.be.server.point.controller.swagger.PointSwaggerDocs;
import kr.hhplus.be.server.point.dto.PointInfo;
import kr.hhplus.be.server.point.service.PointService;
import kr.hhplus.be.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/points")
@RequiredArgsConstructor
public class PointController implements PointSwaggerDocs {
    private final PointService pointService;

    /**
     * 포인트 충전 API
     */
    @PutMapping
    public ApiResponse<PointResponse> chargePoint(
            @RequestBody PointChargeRequest request
    ) {
        PointInfo response = pointService.charge(request.toCommand());
        return ApiResponse.success(ApiResponseCode.SUCCESS_OK_200,PointResponse.from(response));
    }

    /**
     * 포인트 조회 API
     */
    @GetMapping
    public ApiResponse<PointResponse> getPoint(@RequestBody User user) {
        PointResponse response = PointResponse.from(pointService.getPoint(user));
        return ApiResponse.success(ApiResponseCode.SUCCESS_OK_200,response);
    }
}
