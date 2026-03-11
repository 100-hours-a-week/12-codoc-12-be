package _ganzi.codoc.global.ratelimit;

public interface RateLimitStore {

    RateLimitResult tryConsume(String key, RateLimitPolicy policy);
}
