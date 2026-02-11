package _ganzi.codoc.auth.support;

import _ganzi.codoc.auth.domain.AuthUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthUserResolver {

    public AuthUser resolveAuthUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthUser authUser) {
            return authUser;
        }

        return null;
    }
}
