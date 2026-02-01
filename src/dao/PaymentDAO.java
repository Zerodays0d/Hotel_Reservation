package dao;

import model.Payment;

import java.util.List;
import java.util.Optional;


public interface PaymentDAO {
    Payment save(Payment payment);
    Payment update(Payment payment);
    boolean delete(int paymentId);
    Optional<Payment> findById(int paymentId);
    List<Payment> findAll();
    List<Payment> findByReservationId(int reservationId);
}
