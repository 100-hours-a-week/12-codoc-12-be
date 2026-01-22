package _ganzi.codoc.global.dto;

import java.util.List;

public record CursorPagingResponse<T, C>(List<T> items, C nextCursor, boolean hasNextPage) {}
