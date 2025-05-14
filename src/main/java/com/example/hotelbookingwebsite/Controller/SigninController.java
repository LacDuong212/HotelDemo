package com.example.hotelbookingwebsite.Controller;

import com.example.hotelbookingwebsite.Model.Constants;
import com.example.hotelbookingwebsite.Model.Images;
import com.example.hotelbookingwebsite.Model.Manager;
import com.example.hotelbookingwebsite.Model.User;
import com.example.hotelbookingwebsite.Repository.HotelRepository;
import com.example.hotelbookingwebsite.Repository.ImagesRepository;
import com.example.hotelbookingwebsite.Repository.ManagerRepository;
import com.example.hotelbookingwebsite.Service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Objects;

@Controller
@RequestMapping("/signin")
public class SigninController {

    @Autowired
    private UserService userService;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private ImagesRepository imagesRepository;

    @Autowired
    private ManagerRepository managerRepository;

    @GetMapping
    public String showSigninForm(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Email hoặc mật khẩu không đúng");
        }
        return "web/signin";
    }

    @PostMapping
    public String processSignin(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        User user = userService.authenticate(email, password);

        if (user == null) {
            model.addAttribute("error", "Email hoặc mật khẩu không đúng");
            model.addAttribute("email", email);
            return "web/signin";
        }
        Images avatar = imagesRepository.findByOidAndStt(user.getUid(), 0);

        session.setAttribute("user", user);
        session.setAttribute("avatar", avatar);
        String role = user.getRole();

        if (Objects.equals(role, Constants.ROLE.ADMIN)) {
            return "redirect:/admin/manage-hotels";
        } else if (Objects.equals(role, Constants.ROLE.MANAGER)) {
            if (hotelRepository.existsByManager_Uid(user.getUid())) {
                return "redirect:/host/info-hotel";
            } else {
                return "redirect:/host/add-hotel";
            }
        } else {
            return "redirect:/";
        }
    }
}
