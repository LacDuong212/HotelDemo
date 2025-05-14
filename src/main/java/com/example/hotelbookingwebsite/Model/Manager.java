package com.example.hotelbookingwebsite.Model;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
@Table(name = "managers")
public class Manager {
    @Id
    private Long uid;

    @OneToOne
    @MapsId  // manager share same PK: uid
    @JoinColumn(name = "uid")
    private User user;

    @OneToOne(mappedBy = "manager")
    @JsonBackReference
    private Hotel hotel;  // A manager is assigned to a hotel
}
