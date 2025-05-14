package com.example.hotelbookingwebsite.Repository;

import com.example.hotelbookingwebsite.Model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
