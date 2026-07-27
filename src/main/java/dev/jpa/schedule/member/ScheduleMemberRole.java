package dev.jpa.schedule.member;

public final class ScheduleMemberRole {
    private ScheduleMemberRole() {}

    public static final String OWNER  = "OWNER";
    public static final String EDITOR = "EDITOR";
    public static final String VIEWER = "VIEWER";

    public static boolean canEdit(String role) {
        if (role == null) return false;
        String r = role.trim().toUpperCase();
        return OWNER.equals(r) || EDITOR.equals(r);
    }
}
