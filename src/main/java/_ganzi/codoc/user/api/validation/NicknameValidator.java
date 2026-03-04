package _ganzi.codoc.user.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.text.Normalizer;
import java.util.regex.Pattern;

public class NicknameValidator implements ConstraintValidator<Nickname, String> {

    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[A-Za-z0-9가-힣]{2,15}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC);
        if (!NICKNAME_PATTERN.matcher(normalized).matches()) {
            return false;
        }
        // TODO: Reject banned words once the banned-word list is defined.
        return true;
    }
}
