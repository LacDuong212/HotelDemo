package com.example.hotelbookingwebsite.Controller;

import com.example.hotelbookingwebsite.DTO.HotelDTO;
import com.example.hotelbookingwebsite.DTO.UserDTO;
import com.example.hotelbookingwebsite.Model.*;
import com.example.hotelbookingwebsite.Repository.*;
import com.example.hotelbookingwebsite.Service.HotelService;
import com.example.hotelbookingwebsite.Service.PromotionService;
import com.example.hotelbookingwebsite.Service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private HotelService hotelService;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private AdminRepository adminRepository;

    @GetMapping("/manage-hotels")
    public String managehotels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(defaultValue = "hid") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<HotelDTO> hotelPage = hotelService.getAllHotelsPaginated(pageable);

        model.addAttribute("hotels", hotelPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", hotelPage.getTotalPages());
        model.addAttribute("totalItems", hotelPage.getTotalElements());
        model.addAttribute("sortField", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("lastPage", Math.max(0, hotelPage.getTotalPages() - 1));

        return "admin/manage-hotels";
    }

    @DeleteMapping("/delete-hotel/{hotelId}")
    @ResponseBody
    public ResponseEntity<?> deleteHotel(@PathVariable Long hotelId) {
        try {
            hotelRepository.deleteById(hotelId);
            roomRepository.deleteAllByHotel_Hid(hotelId);

            return ResponseEntity.ok().body(Collections.singletonMap("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Lỗi khi xóa khách sạn: " + e.getMessage()));
        }
    }

    @GetMapping("/manage-users")
    public String manageUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "uid") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(defaultValue = "ALL") String role,
            Model model) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserDTO> userPage;
        if (role.equals("ALL")) {
            userPage = userService.getAllUsersPaginated(pageable);
        } else {
            userPage = userService.getUsersByRole(role, pageable);
        }

        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalItems", userPage.getTotalElements());
        model.addAttribute("sortField", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("currentRole", role);

        return "admin/manage-users";
    }

    @PostMapping("/update-users")
    @ResponseBody
    public Map<String, Object> updateUsers(@RequestBody Map<String, List<UserDTO>> payload) {
        List<UserDTO> userDTOList = payload.get("users");

        List<UserDTO> updatedUsers = userService.updateUsers(userDTOList);
        Map<String, Object> response = new HashMap<>();
        if (updatedUsers != null && !updatedUsers.isEmpty()) {
            response.put("success", true);
            response.put("updatedUsers", updatedUsers);
        } else {
            response.put("success", false);
            response.put("message", "No users were updated.");
        }

        return response;
    }

    @DeleteMapping("/delete-user/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return ResponseEntity.ok().body(Collections.singletonMap("success", true));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "User not found"));
        }
    }

    @GetMapping("/manage-voucher")
    public String manageVoucher(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/signin";

        List<Promotion> validPromotions = promotionService.getValidPromotions();
        List<Promotion> expiredPromotions = promotionService.getExpiredPromotions();

        validPromotions = validPromotions.stream()
                .filter(p -> p.getAdmin().getUid().equals(user.getUid()))
                .collect(Collectors.toList());

        expiredPromotions = expiredPromotions.stream()
                .filter(p -> p.getAdmin().getUid().equals(user.getUid()))
                .collect(Collectors.toList());

        model.addAttribute("validPromotions", validPromotions);
        model.addAttribute("expiredPromotions", expiredPromotions);

        return "admin/manage-voucher";
    }

    @PostMapping("/voucher/add")
    public String addPromotion(@ModelAttribute("newPromotion") Promotion promotion,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("user");
        if (loggedInUser == null) {
            return "redirect:/signin";
        }

        Optional<Admin> optionalAdmin = adminRepository.findById(loggedInUser.getUid());
        if (optionalAdmin.isEmpty()) {
            return "redirect:/signin";
        }

        if(promotion.getCode() == null) {
            promotion.setCode(Constants.UNKNOWN);
        }

        promotion.setAdmin(optionalAdmin.get());
        promotion.setStatus(true);
        promotionRepository.save(promotion);

        redirectAttributes.addFlashAttribute("successMessage", "Mã giảm giá đã được thêm thành công!");

        return "redirect:/admin/manage-voucher";
    }

    @GetMapping("/voucher/delete")
    public String deletePromotion(@RequestParam("id") Long promotionId,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("user");
        if (loggedInUser == null) {
            return "redirect:/signin";
        }

        Optional<Admin> optionalAdmin = adminRepository.findById(loggedInUser.getUid());
        if (optionalAdmin.isEmpty()) {
            return "redirect:/signin";
        }

        Optional<Promotion> optionalPromotion = promotionRepository.findById(promotionId);
        if (optionalPromotion.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy mã giảm giá!");
            return "redirect:/admin/manage-voucher";
        }

        Promotion promotion = optionalPromotion.get();

        if (!promotion.getAdmin().getUid().equals(loggedInUser.getUid())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền xóa mã giảm giá này!");
            return "redirect:/admin/manage-voucher";
        }

        promotionRepository.delete(promotion);
        redirectAttributes.addFlashAttribute("successMessage", "Mã giảm giá đã được xóa thành công!");

        return "redirect:/admin/manage-voucher";
    }
}
