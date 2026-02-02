package dao;

import model.User;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for User entity.
 */
public interface UserDAO {
    User save(User user);
    User update(User user);
    boolean delete(int userId);
    Optional<User> findById(int userId);
    Optional<User> findByUsername(String username);
    List<User> findAll();
}
