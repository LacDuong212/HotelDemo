package com.example.hotelbookingwebsite.Controller;

import com.example.hotelbookingwebsite.Model.User;
import com.example.hotelbookingwebsite.Service.EmailService;
import com.example.hotelbookingwebsite.Service.OTPService;
import com.example.hotelbookingwebsite.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/forgot-password")
public class ForgotPasswordController {

    @Autowired
    private UserService userService;

    @Autowired
    private OTPService otpService;

    @Autowired
    private EmailService emailService;

    @GetMapping
    public String showForgotPasswordForm() {
        return "web/forgotpassword";
    }

    @PostMapping
    public String processForgotPassword(@RequestParam String email, Model model) {
        try {
            User user = userService.findByEmail(email);

            // Tạo và gửi OTP
            String otp = otpService.generateOTP(email);
            emailService.sendPasswordResetEmail(email, otp);

            model.addAttribute("email", email);
            return "web/forgotpassword";

        } catch (Exception e) {
            model.addAttribute("error", "Email không tồn tại trong hệ thống");
            return "web/forgotpassword";
        }
    }

    @PostMapping("/verify-otp")
    @ResponseBody
    public ResponseEntity<?> verifyOTP(@RequestParam String email,
                                       @RequestParam String otp) {
        if (otpService.validateOTP(email, otp)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body("Mã OTP không hợp lệ");
    }

    @PostMapping("/reset-password")
    @ResponseBody
    public ResponseEntity<?> resetPassword(@RequestParam String email,
                                           @RequestParam String newPassword) {
        try {
            User user = userService.findByEmail(email);
            userService.updatePassword(user, newPassword);
            otpService.clearOTP(email); // Xóa OTP sau khi đổi mật khẩu thành công
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Đổi mật khẩu thất bại");
        }
    }
}
