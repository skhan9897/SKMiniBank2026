package com.bank.security;

public enum AdminRole {
    SUPER_ADMIN("SUPER_ADMIN", 3),
    ADMIN("ADMIN", 2),
    OPERATOR("OPERATOR", 1);

    private final String roleName;
    private final int level;

    AdminRole(String roleName, int level) {
        this.roleName = roleName;
        this.level = level;
    }

    public String getRoleName() {
        return roleName;
    }

    public int getLevel() {
        return level;
    }

    public static AdminRole fromString(String roleStr) {
        if (roleStr == null) return OPERATOR;
        for (AdminRole r : values()) {
            if (r.roleName.equalsIgnoreCase(roleStr.trim())) {
                return r;
            }
        }
        return OPERATOR;
    }

    public boolean isAtLeast(AdminRole requiredRole) {
        return this.level >= requiredRole.getLevel();
    }
}
