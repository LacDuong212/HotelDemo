package com.example.hotelbookingwebsite.Controller;

import com.example.hotelbookingwebsite.Model.User;
import com.example.hotelbookingwebsite.Service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/account")
public class AccountController {
    private final UserService userService;
    @Autowired
    public AccountController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("")
    public String Account(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser != null) {
            model.addAttribute("user", loggedInUser);
            return "web/account";
        }
        else {
            return "redirect:/signin"; // Chuyển hướng về URL "/signin"
        }
    }
    @GetMapping("/edit-account")
    public String editAccount(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        System.out.println("Edit user profile: " + loggedInUser.getUid());
        model.addAttribute("user", loggedInUser);
        return "web/edit-account";
    }
    @PostMapping("/edit-account")
    public String updateUserProfile(@ModelAttribute User user) {
        System.out.println("Update user profile: " + user.getUid());
        // Gọi dịch vụ để cập nhật thông tin người dùng vào database
        userService.updateUser(user);
        return "redirect:/account";
    }
}
