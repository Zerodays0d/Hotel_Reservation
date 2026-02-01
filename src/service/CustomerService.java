package service;

import dao.CustomerDAO;
import dao.sqlite.SQLiteCustomerDAO;
import model.Customer;

import java.util.List;
import java.util.Optional;

/**
 * Customer business logic.
 */
public class CustomerService {
    private final CustomerDAO customerDAO;

    public CustomerService(CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }

    private static final CustomerService INSTANCE = new CustomerService(new SQLiteCustomerDAO());

    public static CustomerService getInstance() {
        return INSTANCE;
    }

    public int addCustomer(String fullName, String phone, String email, String idNumber) {
        if (fullName == null || fullName.trim().isEmpty()) return -1;
        Customer c = new Customer(0, fullName.trim(), phone != null ? phone : "",
                email != null ? email : "", idNumber != null ? idNumber : "", null, null);
        return customerDAO.save(c).getCustomerId();
    }

    public boolean updateCustomer(int customerId, String fullName, String phone, String email, String idNumber) {
        Optional<Customer> opt = customerDAO.findById(customerId);
        if (opt.isEmpty()) return false;
        Customer c = opt.get();
        if (fullName != null && !fullName.trim().isEmpty()) c.setFullName(fullName.trim());
        if (phone != null) c.setPhone(phone);
        if (email != null) c.setEmail(email);
        if (idNumber != null) c.setIdNumber(idNumber);
        customerDAO.update(c);
        return true;
    }

    public boolean deleteCustomer(int customerId) {
        return customerDAO.delete(customerId);
    }

    public Optional<Customer> findById(int customerId) {
        return customerDAO.findById(customerId);
    }

    public List<Customer> findAll() {
        return customerDAO.findAll();
    }
}
