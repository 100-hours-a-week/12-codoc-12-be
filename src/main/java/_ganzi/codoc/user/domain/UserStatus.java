package _ganzi.codoc.user.domain;

public enum UserStatus {
    ACTIVE,
    ONBOARDING,
    DORMANT,
    DELETED;

    public String asAuthority() {
        return "ROLE_" + name();
    }
}
