package dao;

import model.Room;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Room entity.
 */
public interface RoomDAO {
    Room save(Room room);
    Room update(Room room);
    boolean delete(int roomId);
    Optional<Room> findById(int roomId);
    Optional<Room> findByRoomNumber(String roomNumber);
    List<Room> findAll();
}
