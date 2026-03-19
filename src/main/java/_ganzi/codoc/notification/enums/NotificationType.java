package _ganzi.codoc.notification.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    ATTENDANCE(false, LinkCode.HOME),
    AI_RECOMMENDED_PROBLEM_ISSUED(true, LinkCode.HOME),
    AI_ANALYSIS_REPORT_CREATED(true, LinkCode.MY),
    LEADERBOARD_CLOSED(true, LinkCode.LEADERBOARD),
    LEADERBOARD_STARTED(true, LinkCode.LEADERBOARD),
    CHAT(false, LinkCode.CHAT),
    CUSTOM_PROBLEM_COMPLETED(true, LinkCode.CUSTOM_PROBLEM),
    CUSTOM_PROBLEM_FAILED(true, LinkCode.CUSTOM_PROBLEM);

    private final boolean inboxVisible;
    private final LinkCode linkCode;
}
