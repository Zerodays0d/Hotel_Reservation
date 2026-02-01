package service;

import dao.RoomDAO;
import dao.sqlite.SQLiteRoomDAO;
import model.Room;
import model.RoomStatus;
import model.RoomType;

import java.util.List;
import java.util.Optional;

/**
 * Room business logic.
 */
public class RoomService {
    private final RoomDAO roomDAO;

    public RoomService(RoomDAO roomDAO) {
        this.roomDAO = roomDAO;
    }

    private static final RoomService INSTANCE = new RoomService(new SQLiteRoomDAO());

    public static RoomService getInstance() {
        return INSTANCE;
    }

    public int addRoom(String roomNumber, RoomType roomType, double pricePerNight) {
        if (roomNumber == null || roomNumber.trim().isEmpty()) return -1;
        if (roomDAO.findByRoomNumber(roomNumber.trim()).isPresent()) return -1;
        if (pricePerNight < 0) return -1;
        Room r = new Room(0, roomNumber.trim(), roomType, pricePerNight, RoomStatus.AVAILABLE);
        return roomDAO.save(r).getRoomId();
    }

    public boolean updateRoom(int roomId, String roomNumber, RoomType roomType, double pricePerNight) {
        Optional<Room> opt = roomDAO.findById(roomId);
        if (opt.isEmpty()) return false;
        Room r = opt.get();
        if (roomNumber != null && !roomNumber.trim().isEmpty()) {
            Optional<Room> existing = roomDAO.findByRoomNumber(roomNumber.trim());
            if (existing.isPresent() && existing.get().getRoomId() != roomId) return false;
            r.setRoomNumber(roomNumber.trim());
        }
        if (roomType != null) r.setRoomType(roomType);
        if (pricePerNight >= 0) r.setPricePerNight(pricePerNight);
        roomDAO.update(r);
        return true;
    }

    public boolean deleteRoom(int roomId) {
        Optional<Room> opt = roomDAO.findById(roomId);
        if (opt.isEmpty()) return false;
        if (opt.get().getStatus() == RoomStatus.OCCUPIED) return false;
        return roomDAO.delete(roomId);
    }

    public List<Room> findAll() {
        return roomDAO.findAll();
    }

    public Optional<Room> findById(int roomId) {
        return roomDAO.findById(roomId);
    }

    public Optional<Room> findByRoomNumber(String roomNumber) {
        return roomDAO.findByRoomNumber(roomNumber);
    }

    public void updateRoomStatus(int roomId, RoomStatus status) {
        roomDAO.findById(roomId).ifPresent(r -> {
            r.setStatus(status);
            roomDAO.update(r);
        });
    }
}
