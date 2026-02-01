package service;

import dao.PaymentDAO;
import dao.ReservationDAO;
import dao.sqlite.SQLitePaymentDAO;
import dao.sqlite.SQLiteReservationDAO;
import model.Payment;
import model.PaymentMethod;
import model.Reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Payment business logic.
 */
public class PaymentService {
    private final PaymentDAO paymentDAO;
    private final ReservationDAO reservationDAO;

    public PaymentService(PaymentDAO paymentDAO, ReservationDAO reservationDAO) {
        this.paymentDAO = paymentDAO;
        this.reservationDAO = reservationDAO;
    }

    private static final PaymentService INSTANCE = new PaymentService(
            new SQLitePaymentDAO(), new SQLiteReservationDAO());

    public static PaymentService getInstance() {
        return INSTANCE;
    }

    public int recordPayment(int reservationId, double amount, PaymentMethod method) {
        if (reservationDAO.findById(reservationId).isEmpty()) return -1;
        if (amount <= 0) return -1;
        Payment p = new Payment(0, reservationId, amount, method, LocalDateTime.now());
        return paymentDAO.save(p).getPaymentId();
    }

    public boolean updatePayment(int paymentId, double amount, PaymentMethod method) {
        Optional<Payment> opt = paymentDAO.findById(paymentId);
        if (opt.isEmpty()) return false;
        if (amount <= 0) return false;
        Payment p = opt.get();
        p.setAmount(amount);
        p.setMethod(method);
        paymentDAO.update(p);
        return true;
    }

    public boolean deletePayment(int paymentId) {
        return paymentDAO.delete(paymentId);
    }

    public List<Payment> findAll() {
        return paymentDAO.findAll();
    }

    public List<Payment> findByReservationId(int reservationId) {
        return paymentDAO.findByReservationId(reservationId);
    }

    public List<Payment> findByCustomerId(int customerId) {
        List<Reservation> reservations = reservationDAO.findByCustomerId(customerId);
        return reservations.stream()
                .flatMap(r -> paymentDAO.findByReservationId(r.getReservationId()).stream())
                .toList();
    }

    public Optional<Payment> findById(int paymentId) {
        return paymentDAO.findById(paymentId);
    }
}
