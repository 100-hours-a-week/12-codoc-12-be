package _ganzi.codoc.chatbot.enums;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ChatbotParagraphType {
    BACKGROUND(1),
    GOAL(2),
    STRATEGY(3),
    INSIGHT(4),
    ;

    private final int order;

    public static ChatbotParagraphType getInitialType() {
        return BACKGROUND;
    }

    public ChatbotParagraphType next() {
        int nextOrder = this.order + 1;

        return Arrays.stream(values()).filter(type -> type.order == nextOrder).findFirst().orElse(this);
    }
}
