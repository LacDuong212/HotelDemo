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
@Table(name = "admins")
public class Admin {
    @Id
    private Long uid;

    @OneToOne
    @MapsId   // admin share same PK: uid
    @JoinColumn(name = "uid")
    private User user;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "admin")
    @JsonManagedReference
    private List<Promotion> promotions;  // A manager can have many promotions
}
