package _ganzi.codoc.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChatRoomCreateRequest(
        @NotBlank @Size(max = 100) String title,
        @Size(min = 4, max = 72)
                @Pattern(
                        regexp = "^[\\x20-\\x7E]+$",
                        message = "비밀번호는 영문, 숫자, 특수문자(ASCII)만 사용할 수 있습니다. (한글/이모지 불가)")
                String password) {}
