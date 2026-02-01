package dao.sqlite;

import dao.PaymentDAO;
import model.Payment;
import model.PaymentMethod;
import util.SQLiteConnectionManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class SQLitePaymentDAO implements PaymentDAO {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String INSERT = "INSERT INTO payments (reservation_id, amount, method, payment_date) VALUES (?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE payments SET reservation_id=?, amount=?, method=?, payment_date=? WHERE payment_id=?";
    private static final String DELETE = "DELETE FROM payments WHERE payment_id=?";
    private static final String FIND_BY_ID = "SELECT * FROM payments WHERE payment_id=?";
    private static final String FIND_ALL = "SELECT * FROM payments";
    private static final String FIND_BY_RESERVATION = "SELECT * FROM payments WHERE reservation_id=?";

    @Override
    public Payment save(Payment payment) {
        try (Connection conn = SQLiteConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, payment.getReservationId());
            ps.setDouble(2, payment.getAmount());
            ps.setString(3, payment.getMethod().name());
            ps.setString(4, payment.getPaymentDate() != null ? payment.getPaymentDate().format(FMT) : LocalDateTime.now().format(FMT));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    payment.setPaymentId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save payment", e);
        }
        return payment;
    }

    @Override
    public Payment update(Payment payment) {
        try (Connection conn = SQLiteConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE)) {
            ps.setInt(1, payment.getReservationId());
            ps.setDouble(2, payment.getAmount());
            ps.setString(3, payment.getMethod().name());
            ps.setString(4, payment.getPaymentDate() != null ? payment.getPaymentDate().format(FMT) : LocalDateTime.now().format(FMT));
            ps.setInt(5, payment.getPaymentId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update payment", e);
        }
        return payment;
    }

    @Override
    public boolean delete(int paymentId) {
        try (Connection conn = SQLiteConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE)) {
            ps.setInt(1, paymentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete payment", e);
        }
    }

    @Override
    public Optional<Payment> findById(int paymentId) {
        try (Connection conn = SQLiteConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_ID)) {
            ps.setInt(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find payment", e);
        }
    }

    @Override
    public List<Payment> findAll() {
        List<Payment> list = new ArrayList<>();
        try (Connection conn = SQLiteConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all payments", e);
        }
        return list;
    }

    @Override
    public List<Payment> findByReservationId(int reservationId) {
        List<Payment> list = new ArrayList<>();
        try (Connection conn = SQLiteConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_RESERVATION)) {
            ps.setInt(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find payments by reservation", e);
        }
        return list;
    }

    private Payment mapRow(ResultSet rs) throws SQLException {
        String dateStr = rs.getString("payment_date");
        LocalDateTime dt = dateStr != null ? LocalDateTime.parse(dateStr, FMT) : LocalDateTime.now();
        return new Payment(
                rs.getInt("payment_id"),
                rs.getInt("reservation_id"),
                rs.getDouble("amount"),
                PaymentMethod.valueOf(rs.getString("method")),
                dt
        );
    }
}
