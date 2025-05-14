package com.example.hotelbookingwebsite.DTO;


import com.example.hotelbookingwebsite.Model.Images;
import com.example.hotelbookingwebsite.Model.Manager;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HotelDetailDTO {
    private Long hid;
    private String name;
    private String address;
    private String description;
    private List<Images> image;
    private Manager manager;
    private List<RoomDTO> rooms;
}