package _ganzi.codoc.chat.dto;

public record ChatUnreadStatusBroadcast(long totalUnreadCount) {

    public static ChatUnreadStatusBroadcast of(long totalUnreadCount) {
        return new ChatUnreadStatusBroadcast(totalUnreadCount);
    }
}
