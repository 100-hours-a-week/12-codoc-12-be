package _ganzi.codoc.surprise.dto;

public record SurpriseQuizSubmitResponse(boolean isCorrect, Integer rank, long elapsedMs) {}
