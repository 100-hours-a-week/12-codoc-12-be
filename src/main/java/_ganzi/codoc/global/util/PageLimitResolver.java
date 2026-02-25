package _ganzi.codoc.global.util;

import _ganzi.codoc.global.exception.PageLimitOutOfRangeException;

public final class PageLimitResolver {

    public static final int DEFAULT_LIMIT = 20;
    public static final int DEFAULT_MAX_LIMIT = 50;

    private PageLimitResolver() {}

    public static int resolve(Integer limit) {
        return resolve(limit, DEFAULT_LIMIT, DEFAULT_MAX_LIMIT);
    }

    public static int resolve(Integer limit, int defaultLimit, int maxLimit) {
        if (limit == null) {
            return defaultLimit;
        }

        if (limit < 1 || limit > maxLimit) {
            throw new PageLimitOutOfRangeException();
        }

        return limit;
    }
}
