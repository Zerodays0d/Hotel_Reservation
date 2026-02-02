package dao;

import model.Customer;

import java.util.List;
import java.util.Optional;


public interface CustomerDAO {
    Customer save(Customer customer);
    Customer update(Customer customer);
    boolean delete(int customerId);
    Optional<Customer> findById(int customerId);
    Optional<Customer> findByUsername(String username);
    List<Customer> findAll();
}
