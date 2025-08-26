package kr.hhplus.be.server.common.redisson;

import org.intellij.lang.annotations.Language;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    String topic();

    @Language("SpEL")
    // 락 키
    String keyExpression();

    // 락 획득 대기시간
    long waitTime();

    // 락 점유시간
    long leaseTime();

    // 시간 단위
    TimeUnit unit() default TimeUnit.SECONDS;

}