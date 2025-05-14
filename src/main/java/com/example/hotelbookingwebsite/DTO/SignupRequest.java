package com.example.hotelbookingwebsite.DTO;

import lombok.Data;

@Data
public class SignupRequest {
    private String fullname;
    private String email;
    private String phoneNumber;
    private String password;
    private String confirmPassword;
}
