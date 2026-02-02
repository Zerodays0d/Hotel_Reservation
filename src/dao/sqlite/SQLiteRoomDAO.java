package dao.sqlite;

import dao.RoomDAO;
import model.Room;
import model.RoomStatus;
import model.RoomType;
import util.SQLiteConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQLite implementation of RoomDAO.
 */
public class SQLiteRoomDAO implements RoomDAO {
    private static final String INSERT = "INSERT INTO rooms (room_number, room_type, price_per_night, status) VALUES (?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE rooms SET room_number=?, room_type=?, price_per_night=?, status=? WHERE room_id=?";
    private static final String DELETE = "DELETE FROM rooms WHERE room_id=?";
    private static final String FIND_BY_ID = "SELECT * FROM rooms WHERE room_id=?";
    private static final String FIND_BY_ROOM_NUMBER = "SELECT * FROM rooms WHERE room_number=?";
    private static final String FIND_ALL = "SELECT * FROM rooms";

    @Override
    public Room save(Room room) {
        try (Connection conn = SQLiteConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.getRoomType().name());
            ps.setDouble(3, room.getPricePerNight());
            ps.setString(4, room.getStatus().name());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    room.setRoomId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save room", e);
        }
        return room;
    }

    @Override
    public Room update(Room room) {
        try (Connection conn = SQLiteConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE)) {
            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.getRoomType().name());
            ps.setDouble(3, room.getPricePerNight());
            ps.setString(4, room.getStatus().name());
            ps.setInt(5, room.getRoomId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update room", e);
        }
        return room;
    }

    @Override
    public boolean delete(int roomId) {
        try (Connection conn = SQLiteConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE)) {
            ps.setInt(1, roomId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete room", e);
        }
    }

    @Override
    public Optional<Room> findById(int roomId) {
        try (Connection conn = SQLiteConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_ID)) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find room", e);
        }
    }

    @Override
    public Optional<Room> findByRoomNumber(String roomNumber) {
        try (Connection conn = SQLiteConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_ROOM_NUMBER)) {
            ps.setString(1, roomNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find room by number", e);
        }
    }

    @Override
    public List<Room> findAll() {
        List<Room> list = new ArrayList<>();
        try (Connection conn = SQLiteConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all rooms", e);
        }
        return list;
    }

    private Room mapRow(ResultSet rs) throws SQLException {
        return new Room(
                rs.getInt("room_id"),
                rs.getString("room_number"),
                RoomType.valueOf(rs.getString("room_type")),
                rs.getDouble("price_per_night"),
                RoomStatus.valueOf(rs.getString("status"))
        );
    }
}
