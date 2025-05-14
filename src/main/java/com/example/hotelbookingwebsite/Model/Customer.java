package com.example.hotelbookingwebsite.Model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "customers")
public class Customer {
    @Id
    private Long uid;

    @OneToOne
    @MapsId     // customer share same PK: uid
    @JoinColumn(name = "uid")
    private User user;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "customer")
    @JsonManagedReference
    private List<Booking> bookings;  // A customer can have many bookings
}
