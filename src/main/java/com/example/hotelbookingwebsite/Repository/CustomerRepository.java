package com.example.hotelbookingwebsite.Repository;

import com.example.hotelbookingwebsite.Model.Customer;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUserUid(Long userUid);

    @Transactional
    void deleteByUid(Long uid);
}
