package com.example.hotelbookingwebsite.Model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "hotels")
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hid;

    @Column(nullable = false)
    private String name = Constants.UNKNOWN;

    @Column(nullable = false)
    private String address = Constants.UNKNOWN;

    @Column(length = 1000)
    private String description = Constants.UNKNOWN;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    @OneToOne
    @JoinColumn(name = "uid")
    @JsonManagedReference
    private Manager manager;  // A hotel is managed by one manager

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "hotel")
    @JsonManagedReference
    private List<Room> rooms;  // A hotel can have many rooms
}
