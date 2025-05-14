package com.example.hotelbookingwebsite.Model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rid;

    @Column(nullable = false)
    private String roomName = Constants.UNKNOWN;

    @Column(length = 1000)
    private String description = "";

    @Column(nullable = false)
    private String status = Constants.ROOM_STATUS.UNAVAILABLE;

    @Column(nullable = false)
    private float price = 0;

    @ManyToOne
    @JoinColumn(name = "hid")
    @JsonBackReference
    private Hotel hotel;  // Each room belongs to one hotel

    @OneToMany(mappedBy = "room")
    @JsonBackReference
    private List<Booking> bookings;
}
