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
    ;

    private final boolean inboxVisible;
    private final LinkCode linkCode;
}
