package com.example.hotelbookingwebsite.Controller;

import com.example.hotelbookingwebsite.Model.User;
import com.example.hotelbookingwebsite.Service.BookingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @GetMapping("/booking-payment")
    public String bookingPayment(
            @RequestParam String roomImage,
            @RequestParam Long roomId,
            @RequestParam Double price,
            @RequestParam String roomName,
            @RequestParam String hotelName,
            Model model, HttpSession session) {

        // Lấy UID từ session
        User loggedInUser = (User) session.getAttribute("user");
        if (loggedInUser == null) {
            return "redirect:/signin"; // Nếu uid không tồn tại, chuyển hướng người dùng đến trang đăng nhập
        }

        // Thêm thông tin vào model
        model.addAttribute("roomImage", roomImage);
        model.addAttribute("roomId", roomId);
        model.addAttribute("price", price);
        model.addAttribute("roomName", roomName);
        model.addAttribute("hotelName", hotelName);

        return "web/booking-payment"; // Trả về view tương ứng với trang thanh toán
    }
}
