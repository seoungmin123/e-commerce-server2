package kr.hhplus.be.server.payment.controller;


import kr.hhplus.be.server.payment.dto.PaymentInfo;
import kr.hhplus.be.server.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController implements PaymentControllerDocs {
    private final PaymentService paymentService;

    /**
     * 결제 API
     */
    @PostMapping()
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentCreateRequest request) {
        PaymentInfo paymentInfo = paymentService.pay(request.toCommand());
        return ResponseEntity.ok(PaymentResponse.from(paymentInfo));
    }
}
