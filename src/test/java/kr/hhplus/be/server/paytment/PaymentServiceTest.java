package kr.hhplus.be.server.paytment;


import kr.hhplus.be.server.common.exception.ApiErrorCode;
import kr.hhplus.be.server.common.exception.ApiException;
import kr.hhplus.be.server.payment.domain.IPaymentRepository;
import kr.hhplus.be.server.payment.domain.Payment;
import kr.hhplus.be.server.payment.dto.PaymentCreateCommand;
import kr.hhplus.be.server.payment.dto.PaymentInfo;
import kr.hhplus.be.server.payment.service.PaymentService;
import kr.hhplus.be.server.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock
    private IPaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void 결제_생성시_정상적으로_저장되고_결제정보가_반환된다() {
        // given
        User user = User.create("테스트 유저");
        Long orderId = 1L;
        BigDecimal paymentAmount = BigDecimal.valueOf(10000);
        PaymentCreateCommand command = new PaymentCreateCommand(user, orderId, paymentAmount);

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        PaymentInfo result = paymentService.pay(command);

        // then
        assertThat(result.orderId()).isEqualTo(orderId);
        assertThat(result.paymentAmount()).isEqualTo(paymentAmount);

        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void 결제_금액이_0원_이하인_경우_예외가_발생한다() {
        // given
        User user = User.create("테스트 유저");
        PaymentCreateCommand command = new PaymentCreateCommand(user,1L, BigDecimal.ZERO);

        // when & then
        assertThatThrownBy(() -> paymentService.pay(command))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("apiErrorCode", ApiErrorCode.INVALID_REQUEST);

        verify(paymentRepository, never()).save(any());
    }
}