package com.example.hotelbookingwebsite.Repository;

import com.example.hotelbookingwebsite.Model.Booking;
import com.example.hotelbookingwebsite.Model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByCustomerAndStatus(Customer customer, String status);
    List<Booking> findAllByRoom_Rid(Long rid);
    List<Booking> findByCustomerAndStatusInAndCheckInDateAfter(Customer customer, List<String> statuses, LocalDate checkInDate);
    List<Booking> findByCustomerAndStatusAndCheckInDateLessThanEqual(Customer customer, String status, LocalDate today);
}
