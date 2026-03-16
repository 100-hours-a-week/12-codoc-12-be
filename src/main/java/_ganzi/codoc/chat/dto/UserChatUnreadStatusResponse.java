package _ganzi.codoc.chat.dto;

public record UserChatUnreadStatusResponse(long totalUnreadCount) {

    public static UserChatUnreadStatusResponse from(long totalUnreadCount) {
        return new UserChatUnreadStatusResponse(totalUnreadCount);
    }
}
