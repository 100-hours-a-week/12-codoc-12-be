package _ganzi.codoc.chat.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChatMessageType {
    INIT(true, false, false),
    TEXT(true, true, true),
    SYSTEM(false, true, false),
    ;

    private final boolean visibleInPreview;
    private final boolean visibleInRoom;
    private final boolean senderIdRequired;
}
