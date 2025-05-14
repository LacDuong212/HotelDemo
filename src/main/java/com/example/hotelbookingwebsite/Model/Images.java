package com.example.hotelbookingwebsite.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "images")
public class Images {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long iid;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private Long oid;    // object linked to the image

    private Integer stt = 0;   // for order, default = 0
}