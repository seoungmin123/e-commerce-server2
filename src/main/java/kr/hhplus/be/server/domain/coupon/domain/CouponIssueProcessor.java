package kr.hhplus.be.server.domain.coupon.domain;

import kr.hhplus.be.server.domain.user.domain.IUserRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class CouponIssueProcessor {
    private static final int DEFAULT_BATCH_SIZE = 10000; // 처리 최대수량..임의로 해뒀는데 이게 맞는지는 모르겠음
    private final ICouponRepository couponRepository;
    private final IUserRepository userRepository;

    @Transactional
    public void processCouponIssuance(Coupon coupon) {
        log.info("processIssuanceForCoupon start: {}", coupon.getId());
        int issuableCount = coupon.getTotalIssueQuantity() == null ? DEFAULT_BATCH_SIZE : coupon.getTotalIssueQuantity() - coupon.getIssuedQuantity();
        if (issuableCount <= 0 || couponRepository.getRequestCount(coupon.getId()) <= 0) {
            return;
        } else {
            issuableCount = issuableCount > DEFAULT_BATCH_SIZE ? DEFAULT_BATCH_SIZE : issuableCount;
        }
        // 요청 목록 조회
        Set<ZSetOperations.TypedTuple<Long>> requests = couponRepository.getRequestsByTimestamp(coupon.getId(), issuableCount);

        // 발급 처리
        IssuanceResult result = processRequests(coupon, requests);
        saveResults(coupon.getId(), result, issuableCount);

        log.info("processIssuanceForCoupon end: {}", coupon.getId());
    }

    private IssuanceResult processRequests(Coupon coupon, Set<ZSetOperations.TypedTuple<Long>> requests) {
        IssuanceResult result = new IssuanceResult();

        requests.forEach(request ->
                userRepository.findById(request.getValue()).ifPresent(user -> {
                    try {
                        coupon.issue(user, convertToLocalDateTime(request.getScore()));
                        result.addSuccess(user.getId());
                    } catch (Exception e) {
                        log.error("Failed to issue coupon for user: {}", user.getId(), e);
                        result.addFailure(user.getId());
                    }
                })
        );

        return result;
    }

    private void saveResults(Long couponId, IssuanceResult result, int processedCount) {
        if (!result.getSuccessfulUserIds().isEmpty()) {
            couponRepository.addIssuedCoupon(couponId, result.getSuccessfulUserIds());
            couponRepository.removeRequests(couponId, processedCount);
        }
        if (!result.getFailedUserIds().isEmpty()) {
            couponRepository.addFailures(couponId, result.getFailedUserIds());
        }
    }

    private LocalDateTime convertToLocalDateTime(Double timestamp) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(timestamp.longValue()),
                ZoneId.systemDefault()
        );
    }

    @Getter
    public class IssuanceResult {
        private final List<Long> successfulUserIds = new ArrayList<>();
        private final List<Long> failedUserIds = new ArrayList<>();

        public void addSuccess(Long userId) {
            successfulUserIds.add(userId);
        }

        public void addFailure(Long userId) {
            failedUserIds.add(userId);
        }
    }
}