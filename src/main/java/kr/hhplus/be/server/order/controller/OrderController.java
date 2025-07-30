package kr.hhplus.be.server.order.controller;


import kr.hhplus.be.server.common.response.ApiResponse;
import kr.hhplus.be.server.common.response.ApiResponseCode;
import kr.hhplus.be.server.order.controller.swagger.OrderSwaggerDocs;
import kr.hhplus.be.server.order.facade.OrderFacade;
import kr.hhplus.be.server.order.facade.OrderResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController implements OrderSwaggerDocs {

    private final OrderFacade orderFacade;

    /**
     * 주문/결제 API
     */
    @PostMapping
    public ApiResponse<OrderResponse> createOrder(@RequestBody OrderCreateRequest request) {
        OrderResult result = orderFacade.order(request.toCriteria());
        return ApiResponse.success(ApiResponseCode.SUCCESS_OK_200,OrderResponse.from(result));
    }


}