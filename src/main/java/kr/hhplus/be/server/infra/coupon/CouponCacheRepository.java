package kr.hhplus.be.server.infra.coupon;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;


@Repository
@RequiredArgsConstructor
public class CouponCacheRepository {
    private static final String REQUEST_KEY_FORMAT = "coupon:%d:requests";
    private static final String ISSUED_KEY_FORMAT = "coupon:%d:issued";
    private static final String FAILED_KEY_FORMAT = "coupon:%d:failed";

    private final RedisTemplate<String, Long> redisTemplate;

    public boolean addRequest(Long couponId, Long userId) {
        String requestKey = String.format(REQUEST_KEY_FORMAT, couponId);
        return Boolean.TRUE.equals(redisTemplate.opsForZSet().add(
                requestKey,
                userId,
                System.currentTimeMillis()
        ));
    }

    public int getRequestCount(Long couponId) {
        String requestKey = String.format(REQUEST_KEY_FORMAT, couponId);
        Long size = redisTemplate.opsForZSet().zCard(requestKey);
        return size != null ? size.intValue() : 0;
    }

    public Set<ZSetOperations.TypedTuple<Long>> getRequestsByTimestamp(Long couponId, int limit) {
        String requestKey = String.format(REQUEST_KEY_FORMAT, couponId);
        return redisTemplate.opsForZSet().rangeWithScores(requestKey, 0, limit - 1);
    }

    public void removeRequests(Long couponId, int limit) {
        String requestKey = String.format(REQUEST_KEY_FORMAT, couponId);
        redisTemplate.opsForZSet().removeRange(requestKey, 0, limit - 1);
    }


    public void addIssuance(Long couponId, List<Long> userIds) {
        if (userIds.isEmpty()) {
            return;
        }
        String issuedKey = String.format(ISSUED_KEY_FORMAT, couponId);

        // Pipeline을 사용하여 한 번에 처리
        redisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) {
                userIds.forEach(userId ->
                        operations.opsForSet().add(issuedKey, userId)
                );
                return null;
            }
        });
    }

    public void addFailures(Long couponId, List<Long> userIds) {
        if (userIds.isEmpty()) {
            return;
        }
        String failedKey = String.format(FAILED_KEY_FORMAT, couponId);

        redisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) {
                userIds.forEach(userId ->
                        operations.opsForSet().add(failedKey, userId)
                );
                return null;
            }

        });
    }

    public int getIssuedCount(Long couponId) {
        String issuedKey = String.format(ISSUED_KEY_FORMAT, couponId);
        Long size = redisTemplate.opsForSet().size(issuedKey);
        return size != null ? size.intValue() : 0;
    }

    public boolean isMember(Long couponId, Long userId) {
        String issuedKey = String.format(ISSUED_KEY_FORMAT, couponId);
        return Boolean.TRUE.equals(
                redisTemplate.opsForSet().isMember(issuedKey, userId)
        );
    }

    public int getFailedCount(Long id) {
        String failedKey = String.format(FAILED_KEY_FORMAT, id);
        Long size = redisTemplate.opsForSet().size(failedKey);
        return size != null ? size.intValue() : 0;
    }
}