package com.example.hotelbookingwebsite.DTO;


import com.example.hotelbookingwebsite.Model.Hotel;
import com.example.hotelbookingwebsite.Model.Images;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {

    private Long rid;
    private String description;
    private Float price;
    private String roomName;
    private String status;
    private Hotel hotel;

    private List<String> imageUrl;
}
