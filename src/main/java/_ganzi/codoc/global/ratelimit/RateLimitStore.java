package _ganzi.codoc.global.ratelimit;

import io.github.bucket4j.Bucket;

public interface RateLimitStore {

    /**
     * 지정된 키에 대한 버킷을 가져옵니다. (없으면 생성)
     *
     * @param key 식별자 (userId:apiType)
     * @param policy 적용할 정책 (생성 시 필요)
     * @return Bucket (Local or Distributed)
     */
    Bucket getBucket(String key, RateLimitPolicy policy);
}
