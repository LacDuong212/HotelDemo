package com.example.hotelbookingwebsite.Service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class OTPService {
    private final Map<String, String> otpStorage = new HashMap<>();
    private final Random random = new Random();

    public String generateOTP(String email) {
        String otp = String.format("%06d", random.nextInt(999999));
        otpStorage.put(email, otp);
        return otp;
    }

    public boolean validateOTP(String email, String otp) {
        String storedOtp = otpStorage.get(email);
        return storedOtp != null && storedOtp.equals(otp);
    }

    public void clearOTP(String email) {
        otpStorage.remove(email);
    }
}
