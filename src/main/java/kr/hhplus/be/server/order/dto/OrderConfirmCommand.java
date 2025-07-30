package kr.hhplus.be.server.order.dto;

public record OrderConfirmCommand(
        Long orderId
) {
    public static OrderConfirmCommand from(Long orderId) {
        return new OrderConfirmCommand(orderId);
    }
}
