package _ganzi.codoc.surprise.enums;

public enum SubmissionReservationStatus {
    ACCEPTED,
    DUPLICATE,
    EXHAUSTED;

    public static SubmissionReservationStatus fromValue(String value) {
        try {
            return SubmissionReservationStatus.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("Unexpected surprise reservation status.");
        }
    }
}
