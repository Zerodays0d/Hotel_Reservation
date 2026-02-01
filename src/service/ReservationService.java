package service;

import dao.ReservationDAO;
import dao.RoomDAO;
import dao.sqlite.SQLiteReservationDAO;
import dao.sqlite.SQLiteRoomDAO;
import model.Reservation;
import model.ReservationStatus;
import model.Room;
import model.RoomStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Reservation business logic.
 * Checks room availability, validates dates, prevents double booking, updates room status.
 */
public class ReservationService {
    private final ReservationDAO reservationDAO;
    private final RoomDAO roomDAO;

    public ReservationService(ReservationDAO reservationDAO, RoomDAO roomDAO) {
        this.reservationDAO = reservationDAO;
        this.roomDAO = roomDAO;
    }

    private static final ReservationService INSTANCE = new ReservationService(
            new SQLiteReservationDAO(), new SQLiteRoomDAO());

    public static ReservationService getInstance() {
        return INSTANCE;
    }

    public int createReservation(int customerId, int roomId, LocalDate checkIn, LocalDate checkOut, int numberOfGuests) {
        String validation = validateReservation(null, customerId, roomId, checkIn, checkOut);
        if (validation != null) return -1;
        if (numberOfGuests < 1) numberOfGuests = 1;

        Reservation r = new Reservation(0, customerId, roomId, checkIn, checkOut, numberOfGuests, ReservationStatus.BOOKED);
        reservationDAO.save(r);
        roomDAO.findById(roomId).ifPresent(room -> {
            room.setStatus(RoomStatus.OCCUPIED);
            roomDAO.update(room);
        });
        return r.getReservationId();
    }

    public boolean updateReservation(int reservationId, int customerId, int roomId, LocalDate checkIn, LocalDate checkOut, int numberOfGuests) {
        Optional<Reservation> opt = reservationDAO.findById(reservationId);
        if (opt.isEmpty()) return false;
        String validation = validateReservation(reservationId, customerId, roomId, checkIn, checkOut);
        if (validation != null) return false;

        Reservation r = opt.get();
        int oldRoomId = r.getRoomId();
        r.setCustomerId(customerId);
        r.setRoomId(roomId);
        r.setCheckInDate(checkIn);
        r.setCheckOutDate(checkOut);
        r.setNumberOfGuests(numberOfGuests > 0 ? numberOfGuests : 1);
        reservationDAO.update(r);

        if (oldRoomId != roomId) {
            roomDAO.findById(oldRoomId).ifPresent(room -> {
                if (!hasActiveReservation(oldRoomId, reservationId)) {
                    room.setStatus(RoomStatus.AVAILABLE);
                    roomDAO.update(room);
                }
            });
        }
        roomDAO.findById(roomId).ifPresent(room -> {
            room.setStatus(RoomStatus.OCCUPIED);
            roomDAO.update(room);
        });
        return true;
    }

    public boolean cancelReservation(int reservationId) {
        Optional<Reservation> opt = reservationDAO.findById(reservationId);
        if (opt.isEmpty()) return false;
        Reservation r = opt.get();
        if (r.getStatus() == ReservationStatus.CANCELLED) return true;
        r.setStatus(ReservationStatus.CANCELLED);
        reservationDAO.update(r);
        int roomId = r.getRoomId();
        if (!hasActiveReservation(roomId, reservationId)) {
            roomDAO.findById(roomId).ifPresent(room -> {
                room.setStatus(RoomStatus.AVAILABLE);
                roomDAO.update(room);
            });
        }
        return true;
    }

    public boolean checkIn(int reservationId) {
        Optional<Reservation> opt = reservationDAO.findById(reservationId);
        if (opt.isEmpty()) return false;
        Reservation r = opt.get();
        if (r.getStatus() != ReservationStatus.BOOKED) return false;
        r.setStatus(ReservationStatus.CHECKED_IN);
        reservationDAO.update(r);
        return true;
    }

    public boolean checkOut(int reservationId) {
        Optional<Reservation> opt = reservationDAO.findById(reservationId);
        if (opt.isEmpty()) return false;
        Reservation r = opt.get();
        if (r.getStatus() != ReservationStatus.CHECKED_IN) return false;
        r.setStatus(ReservationStatus.COMPLETED);
        reservationDAO.update(r);
        int roomId = r.getRoomId();
        if (!hasActiveReservation(roomId, reservationId)) {
            roomDAO.findById(roomId).ifPresent(room -> {
                room.setStatus(RoomStatus.AVAILABLE);
                roomDAO.update(room);
            });
        }
        return true;
    }

    private boolean hasActiveReservation(int roomId, int excludeReservationId) {
        List<Reservation> list = reservationDAO.findByRoomId(roomId);
        return list.stream()
                .filter(r -> r.getReservationId() != excludeReservationId)
                .anyMatch(r -> r.getStatus() == ReservationStatus.BOOKED || r.getStatus() == ReservationStatus.CHECKED_IN);
    }

    public String validateReservation(Integer excludeReservationId, int customerId, int roomId,
                                     LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) return "Dates required";
        if (!checkOut.isAfter(checkIn)) return "Check-out must be after check-in";
        if (checkIn.isBefore(LocalDate.now())) return "Check-in cannot be in the past";
        if (roomDAO.findById(roomId).isEmpty()) return "Room not found";
        if (!reservationDAO.findOverlappingReservations(roomId, checkIn, checkOut, excludeReservationId).isEmpty()) {
            return "Room is already booked for these dates";
        }
        return null;
    }

    public List<Reservation> findAll() {
        return reservationDAO.findAll();
    }

    public Optional<Reservation> findById(int reservationId) {
        return reservationDAO.findById(reservationId);
    }

    public List<Reservation> findByCustomerId(int customerId) {
        return reservationDAO.findByCustomerId(customerId);
    }

    public List<Room> getAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        List<Room> all = roomDAO.findAll();
        return all.stream().filter(r -> reservationDAO.findOverlappingReservations(
                r.getRoomId(), checkIn, checkOut, null).isEmpty()).toList();
    }
}
