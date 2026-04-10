package _ganzi.codoc.surprise.dto;

import _ganzi.codoc.surprise.enums.SubmissionReservationStatus;

public record SubmissionReservation(
        SubmissionReservationStatus status, Integer rankNo, int markerValue) {}
