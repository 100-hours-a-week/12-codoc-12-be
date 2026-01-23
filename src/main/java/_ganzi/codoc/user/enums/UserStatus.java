package _ganzi.codoc.user.enums;

public enum UserStatus {
    ACTIVE,
    ONBOARDING,
    DORMANT,
    DELETED;

    public String asAuthority() {
        return "ROLE_" + name();
    }
}
