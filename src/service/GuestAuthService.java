package service;

import dao.CustomerDAO;
import dao.sqlite.SQLiteCustomerDAO;
import model.Customer;

import java.util.Optional;

/**
 * Guest (customer) authentication. Guests log in with username/password stored in customers table.
 */
public class GuestAuthService {
    private final CustomerDAO customerDAO;

    public GuestAuthService(CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }

    private static final GuestAuthService INSTANCE = new GuestAuthService(new SQLiteCustomerDAO());

    public static GuestAuthService getInstance() {
        return INSTANCE;
    }

    public int login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null) return -1;
        Optional<Customer> opt = customerDAO.findByUsername(username.trim());
        if (opt.isEmpty()) return -1;
        Customer c = opt.get();
        if (c.getPasswordHash() == null || c.getPasswordHash().isEmpty()) return -1;
        String hash = AuthService.getInstance().hashPasswordForGuest(password);
        if (!hash.equals(c.getPasswordHash())) return -1;
        SessionManager.loginGuest(c.getCustomerId(), c.getFullName());
        return c.getCustomerId();
    }

    public int register(String username, String fullName, String phone, String email, String idNumber, String password) {
        if (username == null || username.trim().isEmpty()) return -1;
        if (fullName == null || fullName.trim().isEmpty()) return -1;
        if (password == null || password.isEmpty()) return -1;
        if (customerDAO.findByUsername(username.trim()).isPresent()) return -1;
        String hash = AuthService.getInstance().hashPasswordForGuest(password);
        Customer c = new Customer(0, fullName.trim(), phone != null ? phone : "",
                email != null ? email : "", idNumber != null ? idNumber : "",
                username.trim(), hash);
        customerDAO.save(c);
        SessionManager.loginGuest(c.getCustomerId(), c.getFullName());
        return c.getCustomerId();
    }
}
