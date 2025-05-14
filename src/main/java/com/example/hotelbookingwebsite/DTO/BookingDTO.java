package com.example.hotelbookingwebsite.DTO;

import com.example.hotelbookingwebsite.Model.Customer;
import com.example.hotelbookingwebsite.Model.Payment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingDTO {
    private Long bid;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String status;
    private float totalPrice;

    private Customer customer;     // Bạn có thể tạo thêm CustomerDTO nếu muốn tách rõ DTO và Entity
    private Payment payment;       // Tương tự như trên, có thể dùng PaymentDTO nếu cần
    private RoomDTO room;          // Chỉ khác phần này là RoomDTO
}
