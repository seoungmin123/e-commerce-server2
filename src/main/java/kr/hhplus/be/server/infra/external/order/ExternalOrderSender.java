package kr.hhplus.be.server.infra.external.order;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExternalOrderSender {

    public void send(String event) {
        // 단순 로그 출력
        try {
            log.info("[외부 전송 성공] - event: {}", event);
        } catch (Exception e) {
            log.error("[외부 전송 실패] - event: {}", event, e);
            throw e;
        }
    }
}