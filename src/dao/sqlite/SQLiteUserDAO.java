package dao.sqlite;

import dao.UserDAO;
import model.User;
import util.SQLiteConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteUserDAO implements UserDAO {
    private static final String INSERT = "INSERT INTO users (username, password_hash, full_name, is_active) VALUES (?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE users SET username=?, password_hash=?, full_name=?, is_active=? WHERE user_id=?";
    private static final String DELETE = "DELETE FROM users WHERE user_id=?";
    private static final String FIND_BY_ID = "SELECT * FROM users WHERE user_id=?";
    private static final String FIND_BY_USERNAME = "SELECT * FROM users WHERE username=?";
    private static final String FIND_ALL = "SELECT * FROM users";

    @Override
    public User save(User user) {
        try (Connection conn = SQLiteConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName());
            ps.setInt(4, user.isActive() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setUserId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save user", e);
        }
        return user;
    }

    @Override
    public User update(User user) {
        try (Connection conn = SQLiteConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName());
            ps.setInt(4, user.isActive() ? 1 : 0);
            ps.setInt(5, user.getUserId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user", e);
        }
        return user;
    }

    @Override
    public boolean delete(int userId) {
        try (Connection conn = SQLiteConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    @Override
    public Optional<User> findById(int userId) {
        try (Connection conn = SQLiteConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_ID)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user", e);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        try (Connection conn = SQLiteConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_USERNAME)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by username", e);
        }
    }

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        try (Connection conn = SQLiteConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all users", e);
        }
        return list;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("full_name"),
                rs.getInt("is_active") == 1
        );
    }
}
