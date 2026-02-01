package dao.sqlite;

import dao.CustomerDAO;
import model.Customer;
import util.SQLiteConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class SQLiteCustomerDAO implements CustomerDAO {
    private static final String INSERT = "INSERT INTO customers (full_name, phone, email, id_number, username, password_hash) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE customers SET full_name=?, phone=?, email=?, id_number=?, username=?, password_hash=? WHERE customer_id=?";
    private static final String DELETE = "DELETE FROM customers WHERE customer_id=?";
    private static final String FIND_BY_ID = "SELECT * FROM customers WHERE customer_id=?";
    private static final String FIND_BY_USERNAME = "SELECT * FROM customers WHERE username=? AND username IS NOT NULL AND username != ''";
    private static final String FIND_ALL = "SELECT * FROM customers";

    @Override
    public Customer save(Customer customer) {
        try (Connection conn = SQLiteConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, customer.getFullName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getIdNumber());
            ps.setString(5, customer.getUsername());
            ps.setString(6, customer.getPasswordHash());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    customer.setCustomerId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save customer", e);
        }
        return customer;
    }

    @Override
    public Customer update(Customer customer) {
        try (Connection conn = SQLiteConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE)) {
            ps.setString(1, customer.getFullName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getIdNumber());
            ps.setString(5, customer.getUsername());
            ps.setString(6, customer.getPasswordHash());
            ps.setInt(7, customer.getCustomerId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update customer", e);
        }
        return customer;
    }

    @Override
    public boolean delete(int customerId) {
        try (Connection conn = SQLiteConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE)) {
            ps.setInt(1, customerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete customer", e);
        }
    }

    @Override
    public Optional<Customer> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) return Optional.empty();
        try (Connection conn = SQLiteConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_USERNAME)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find customer by username", e);
        }
    }

    @Override
    public Optional<Customer> findById(int customerId) {
        try (Connection conn = SQLiteConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_ID)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find customer", e);
        }
    }

    @Override
    public List<Customer> findAll() {
        List<Customer> list = new ArrayList<>();
        try (Connection conn = SQLiteConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all customers", e);
        }
        return list;
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        String username = null, passwordHash = null;
        try {
            username = rs.getString("username");
            passwordHash = rs.getString("password_hash");
        } catch (SQLException ignored) { }
        return new Customer(
                rs.getInt("customer_id"),
                rs.getString("full_name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("id_number"),
                username,
                passwordHash
        );
    }
}
