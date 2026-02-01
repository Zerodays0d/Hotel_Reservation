package service;

import dao.UserDAO;
import dao.sqlite.SQLiteUserDAO;
import model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

/**
 * Authentication business logic.
 * Depends on DAO interfaces only (Dependency Inversion).
 */
public class AuthService {
    private static volatile int currentUserId = -1;
    private static volatile String currentUsername;
    private final UserDAO userDAO;

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    private static final AuthService INSTANCE = new AuthService(new SQLiteUserDAO());

    public static AuthService getInstance() {
        return INSTANCE;
    }

    public int login(String username, String password) {
        int id = doLogin(username, password);
        if (id != -1) SessionManager.loginAdmin(id, userDAO.findByUsername(username.trim()).map(User::getFullName).orElse(username));
        return id;
    }

    private int doLogin(String username, String password) {
        Optional<User> opt = userDAO.findByUsername(username.trim());
        if (opt.isEmpty()) return -1;
        User u = opt.get();
        if (!u.isActive()) return -1;
        String hash = hashPassword(password);
        if (!hash.equals(u.getPasswordHash())) return -1;
        currentUserId = u.getUserId();
        currentUsername = u.getUsername();
        return currentUserId;
    }

    /** For guest auth - same hashing. */
    public String hashPasswordForGuest(String password) {
        return hashPassword(password);
    }

    public int register(String username, String fullName, String password) {
        if (username == null || username.trim().isEmpty()) return -1;
        if (userDAO.findByUsername(username.trim()).isPresent()) return -1;
        User u = new User(0, username.trim(), hashPassword(password), fullName != null ? fullName : username, true);
        userDAO.save(u);
        SessionManager.loginAdmin(u.getUserId(), u.getFullName());
        currentUserId = u.getUserId();
        currentUsername = u.getUsername();
        return currentUserId;
    }

    /** Seeds default admin if no users exist. Does not log in. */
    public static void seedAdminIfNeeded() {
        AuthService svc = getInstance();
        if (!svc.userDAO.findAll().isEmpty()) return;
        User u = new User(0, "admin", svc.hashPassword("admin"), "Administrator", true);
        svc.userDAO.save(u);
    }

    public void logout() {
        currentUserId = -1;
        currentUsername = null;
        SessionManager.logout();
    }

    public static int getCurrentUserId() {
        return currentUserId;
    }

    public static String getCurrentUsername() {
        return currentUsername;
    }

    public boolean changePassword(int userId, String currentPassword, String newPassword) {
        Optional<User> opt = userDAO.findById(userId);
        if (opt.isEmpty()) return false;
        User u = opt.get();
        if (!u.getPasswordHash().equals(hashPassword(currentPassword))) return false;
        u.setPasswordHash(hashPassword(newPassword));
        userDAO.update(u);
        return true;
    }

    String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
