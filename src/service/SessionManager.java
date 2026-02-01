package service;

/**
 * Manages current session: user type (ADMIN or GUEST) and ID.
 */
public final class SessionManager {
    public enum UserType { ADMIN, GUEST }

    private static volatile UserType currentType;
    private static volatile int currentAdminId = -1;
    private static volatile int currentGuestId = -1;
    private static volatile String currentDisplayName;

    public static void loginAdmin(int userId, String displayName) {
        currentType = UserType.ADMIN;
        currentAdminId = userId;
        currentGuestId = -1;
        currentDisplayName = displayName;
    }

    public static void loginGuest(int customerId, String displayName) {
        currentType = UserType.GUEST;
        currentGuestId = customerId;
        currentAdminId = -1;
        currentDisplayName = displayName;
    }

    public static void logout() {
        currentType = null;
        currentAdminId = -1;
        currentGuestId = -1;
        currentDisplayName = null;
    }

    public static UserType getCurrentType() {
        return currentType;
    }

    public static boolean isAdmin() {
        return currentType == UserType.ADMIN;
    }

    public static boolean isGuest() {
        return currentType == UserType.GUEST;
    }

    public static int getCurrentAdminId() {
        return currentAdminId;
    }

    public static int getCurrentGuestId() {
        return currentGuestId;
    }

    public static String getCurrentDisplayName() {
        return currentDisplayName;
    }

    /** For guests, returns customer ID. For admins, returns -1. */
    public static int getCurrentCustomerIdForReservation() {
        return currentGuestId;
    }
}
