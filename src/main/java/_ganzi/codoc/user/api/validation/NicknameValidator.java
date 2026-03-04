package _ganzi.codoc.user.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class NicknameValidator implements ConstraintValidator<Nickname, String> {

    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[A-Za-z0-9가-힣]{2,15}$");

    private final BannedWordMatcher bannedWordMatcher;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        String normalized = normalize(value);
        if (!NICKNAME_PATTERN.matcher(normalized).matches()) {
            return false;
        }
        String bannedWord = bannedWordMatcher.findFirstMatch(normalized);
        if (bannedWord != null) {
            context.disableDefaultConstraintViolation();
            context
                    .buildConstraintViolationWithTemplate("금지어 포함: " + bannedWord)
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

    private String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
    }
}
