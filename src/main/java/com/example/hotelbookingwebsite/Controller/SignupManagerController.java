package com.example.hotelbookingwebsite.Controller;

import com.example.hotelbookingwebsite.DTO.SignupRequest;
import com.example.hotelbookingwebsite.Model.Constants;
import com.example.hotelbookingwebsite.Model.Manager;
import com.example.hotelbookingwebsite.Model.User;
import com.example.hotelbookingwebsite.Service.EmailService;
import com.example.hotelbookingwebsite.Service.OTPService;
import com.example.hotelbookingwebsite.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/signup-manager")
public class SignupManagerController {
    @Autowired
    private UserService userService;

    @Autowired
    private OTPService otpService;

    @Autowired
    private EmailService emailService;

    @GetMapping
    public String showSignupForm() {
        return "host/signup-manager";
    }

    @PostMapping
    public String processSignup(SignupRequest signupRequest, Model model) {
        // Kiểm tra mật khẩu
        if (!signupRequest.getPassword().equals(signupRequest.getConfirmPassword())) {
            model.addAttribute("error", "Mật khẩu không trùng khớp");
            return "host/signup-manager";
        }

        // Kiểm tra email đã tồn tại chưa
        if (userService.existsByEmail(signupRequest.getEmail())) {
            model.addAttribute("error", "Email đã được sử dụng");
            return "host/signup-manager";
        }

        // Kiểm tra số điện thoại
        if (userService.existsByPhoneNumber(signupRequest.getPhoneNumber())) {
            model.addAttribute("error", "Số điện thoại đã được sử dụng");
            return "host/signup-manager";
        }

        // Tạo OTP và gửi email
        String otp = otpService.generateOTP(signupRequest.getEmail());
        emailService.sendOTPEmail(signupRequest.getEmail(), otp);

        // Truyền dữ liệu vào model để gửi lại client
        model.addAttribute("email", signupRequest.getEmail());
        model.addAttribute("fullname", signupRequest.getFullname());
        model.addAttribute("phoneNumber", signupRequest.getPhoneNumber());
        model.addAttribute("password", signupRequest.getPassword());

        return "host/signup-manager";
    }

    @PostMapping("/verify-otp")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verifyOTP(
            @RequestParam String email,
            @RequestParam String otp,
            @RequestParam String fullname,
            @RequestParam String phoneNumber,
            @RequestParam String password) {

        Map<String, Object> response = new HashMap<>();

        if (!otpService.validateOTP(email, otp)) {
            response.put("success", false);
            response.put("message", "Mã OTP không hợp lệ");
            return ResponseEntity.ok(response);
        }

        // Tạo và lưu user sau khi OTP hợp lệ
        User user = new User();
        user.setEmail(email);
        user.setFullname(fullname);
        user.setPhoneNumber(phoneNumber);
        user.setPassword(password); // nên mã hóa trước khi lưu nếu có dùng encoder
        user.setRole(Constants.ROLE.MANAGER);
        userService.saveUser(user);

        // Lưu customer
        Manager manager = new Manager();
        manager.setUser(user);
        userService.saveManager(manager);

        otpService.clearOTP(email);

        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-otp")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resendOTP(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();

        try {
            String otp = otpService.generateOTP(email);
            emailService.sendOTPEmail(email, otp);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi gửi OTP: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}
