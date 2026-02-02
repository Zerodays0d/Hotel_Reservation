package dao;

import model.Reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Reservation entity.
 */
public interface ReservationDAO {
    Reservation save(Reservation reservation);
    Reservation update(Reservation reservation);
    boolean delete(int reservationId);
    Optional<Reservation> findById(int reservationId);
    List<Reservation> findAll();
    List<Reservation> findByRoomId(int roomId);
    List<Reservation> findByCustomerId(int customerId);
    List<Reservation> findOverlappingReservations(int roomId, LocalDate checkIn, LocalDate checkOut, Integer excludeReservationId);
}
