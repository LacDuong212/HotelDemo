package com.example.hotelbookingwebsite.Model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bid;

    @Column(nullable = false)
    private LocalDate checkInDate = LocalDate.now();    // default value

    private LocalDate checkOutDate;

    @Column(nullable = false)
    private String status = Constants.UNKNOWN;

    private float totalPrice = 0;

    @ManyToOne
    @JoinColumn(name = "uid")
    @JsonBackReference
    private Customer customer;  // A booking belongs to one customer

    @OneToOne
    @JoinColumn(name = "pmid")
    @JsonManagedReference
    private Payment payment;  // Each booking has one payment

    @ManyToOne
    @JoinColumn(name = "rid")
    @JsonManagedReference
    private Room room;  // Each booking is associated with one room
}
