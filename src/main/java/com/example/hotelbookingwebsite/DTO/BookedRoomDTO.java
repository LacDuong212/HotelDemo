package com.example.hotelbookingwebsite.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookedRoomDTO {
    private RoomDTO room;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String customerName;
    private String status;
}
