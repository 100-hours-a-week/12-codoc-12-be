package _ganzi.codoc.custom.infra;

import _ganzi.codoc.custom.exception.CustomProblemInvalidFileKeyException;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CustomProblemObjectKeyPolicy {

    private static final String KEY_PREFIX = "custom-problems";
    private static final String USER_KEY_PREFIX = "user-";

    public String generate(Long userId) {
        return userNamespace(userId) + "/" + UUID.randomUUID();
    }

    public void validateKeyNamespace(Long userId, String fileKey) {
        String expectedPrefix = userNamespace(userId) + "/";
        if (fileKey == null || fileKey.isBlank() || !fileKey.startsWith(expectedPrefix)) {
            throw new CustomProblemInvalidFileKeyException();
        }
    }

    private String userNamespace(Long userId) {
        return KEY_PREFIX + "/" + USER_KEY_PREFIX + userId;
    }
}
