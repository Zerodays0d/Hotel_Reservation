package dao.sqlite;

import dao.ReservationDAO;
import model.Reservation;
import model.ReservationStatus;
import util.SQLiteConnectionManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteReservationDAO implements ReservationDAO {
    private static final String INSERT = "INSERT INTO reservations (customer_id, room_id, check_in_date, check_out_date, number_of_guests, status) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE reservations SET customer_id=?, room_id=?, check_in_date=?, check_out_date=?, number_of_guests=?, status=? WHERE reservation_id=?";
    private static final String DELETE = "DELETE FROM reservations WHERE reservation_id=?";
    private static final String FIND_BY_ID = "SELECT * FROM reservations WHERE reservation_id=?";
    private static final String FIND_ALL = "SELECT * FROM reservations";
    private static final String FIND_BY_ROOM_ID = "SELECT * FROM reservations WHERE room_id=?";
    private static final String FIND_BY_CUSTOMER_ID = "SELECT * FROM reservations WHERE customer_id=?";
    private static final String FIND_OVERLAPPING = "SELECT * FROM reservations WHERE room_id=? AND status NOT IN ('CANCELLED') " +
            "AND check_in_date < ? AND check_out_date > ?";

    @Override
    public Reservation save(Reservation reservation) {
        try (Connection conn = SQLiteConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reservation.getCustomerId());
            ps.setInt(2, reservation.getRoomId());
            ps.setString(3, reservation.getCheckInDate().toString());
            ps.setString(4, reservation.getCheckOutDate().toString());
            ps.setInt(5, reservation.getNumberOfGuests());
            ps.setString(6, reservation.getStatus().name());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    reservation.setReservationId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save reservation", e);
        }
        return reservation;
    }

    @Override
    public Reservation update(Reservation reservation) {
        try (Connection conn = SQLiteConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE)) {
            ps.setInt(1, reservation.getCustomerId());
            ps.setInt(2, reservation.getRoomId());
            ps.setString(3, reservation.getCheckInDate().toString());
            ps.setString(4, reservation.getCheckOutDate().toString());
            ps.setInt(5, reservation.getNumberOfGuests());
            ps.setString(6, reservation.getStatus().name());
            ps.setInt(7, reservation.getReservationId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update reservation", e);
        }
        return reservation;
    }

    @Override
    public boolean delete(int reservationId) {
        try (Connection conn = SQLiteConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE)) {
            ps.setInt(1, reservationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete reservation", e);
        }
    }

    @Override
    public Optional<Reservation> findById(int reservationId) {
        try (Connection conn = SQLiteConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_ID)) {
            ps.setInt(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find reservation", e);
        }
    }

    @Override
    public List<Reservation> findAll() {
        List<Reservation> list = new ArrayList<>();
        try (Connection conn = SQLiteConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all reservations", e);
        }
        return list;
    }

    @Override
    public List<Reservation> findByRoomId(int roomId) {
        return findByInt(FIND_BY_ROOM_ID, roomId);
    }

    @Override
    public List<Reservation> findByCustomerId(int customerId) {
        return findByInt(FIND_BY_CUSTOMER_ID, customerId);
    }

    @Override
    public List<Reservation> findOverlappingReservations(int roomId, LocalDate checkIn, LocalDate checkOut, Integer excludeReservationId) {
        List<Reservation> list = new ArrayList<>();
        try (Connection conn = SQLiteConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_OVERLAPPING)) {
            ps.setInt(1, roomId);
            ps.setString(2, checkOut.toString());  // existing.check_in < new.checkOut
            ps.setString(3, checkIn.toString());   // existing.check_out > new.checkIn
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reservation r = mapRow(rs);
                    if (excludeReservationId == null || r.getReservationId() != excludeReservationId) {
                        list.add(r);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find overlapping reservations", e);
        }
        return list;
    }

    private List<Reservation> findByInt(String sql, int value) {
        List<Reservation> list = new ArrayList<>();
        try (Connection conn = SQLiteConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find reservations", e);
        }
        return list;
    }

    private Reservation mapRow(ResultSet rs) throws SQLException {
        int numGuests = 1;
        try {
            numGuests = rs.getInt("number_of_guests");
            if (numGuests < 1) numGuests = 1;
        } catch (SQLException ignored) { }
        return new Reservation(
                rs.getInt("reservation_id"),
                rs.getInt("customer_id"),
                rs.getInt("room_id"),
                LocalDate.parse(rs.getString("check_in_date")),
                LocalDate.parse(rs.getString("check_out_date")),
                numGuests,
                ReservationStatus.valueOf(rs.getString("status"))
        );
    }
}
