package _ganzi.codoc.global.exception;

import lombok.Getter;

@Getter
public class RateLimitExceededException extends BaseException {

    public RateLimitExceededException() {
        super(GlobalErrorCode.RATE_LIMIT_EXCEEDED);
    }
}
