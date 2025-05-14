package com.example.hotelbookingwebsite.Controller;

import com.example.hotelbookingwebsite.DTO.HotelDTO;
import com.example.hotelbookingwebsite.DTO.HotelDetailDTO;
import com.example.hotelbookingwebsite.Model.*;
import com.example.hotelbookingwebsite.Repository.ImagesRepository;
import com.example.hotelbookingwebsite.Service.*;
import jakarta.servlet.http.HttpSession;
import com.example.hotelbookingwebsite.Service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    @Autowired
    private ImagesRepository imagesRepository;

    @Autowired
    private ImageService imageService;

    private final HotelService hotelService;
    private final HotelDetailService hotelDetailService;
    private final RoomService roomService;
    private final BookingService bookingService;
    private final PromotionService promotionService;

    @Autowired
    public HomeController(HotelService hotelService, HotelDetailService hotelDetailService, RoomService roomService, BookingService bookingService, PromotionService promotionService) {
        this.hotelService = hotelService;
        this.hotelDetailService = hotelDetailService;
        this.roomService = roomService;
        this.bookingService = bookingService;
        this.promotionService = promotionService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("newestHotels", hotelService.getNewestHotels());
        model.addAttribute("hcmHotels", hotelService.getTop4NewestHotelsInHCM());
        return "web/home";
    }

    @GetMapping("/hotel-detail/{id}")
    public String hoteldetail(@PathVariable("id") long id, Model model) {
        model.addAttribute("hotel", hotelDetailService.getHotelById(id));
        return "web/hotel-detail";
    }

    @GetMapping("/room-detail/{id}")
    public String roomdetail(@PathVariable("id") long id, Model model) {
        model.addAttribute("room",roomService.getRoomDTOById(id));
        return "web/room-detail";
    }

    @GetMapping("/booking-history")
    public String bookingHistory(HttpSession session,Model model) {
        User loggedInUser = (User) session.getAttribute("user");
        if (loggedInUser != null) {
            model.addAttribute("upcomingBookings", bookingService.getUpcomingBookings(loggedInUser.getUid()));
            model.addAttribute("paidBookings", bookingService.getPaidBookings(loggedInUser.getUid()));
            model.addAttribute("cancelBookings", bookingService.getBookingByUidAndStatus(loggedInUser.getUid(), Constants.PAYMENT_STATUS.REFUNDED));
            return "web/booking-history";
        }
        else {
            return "redirect:/signin"; // Chuyển hướng về URL "/signin"
        }
    }
    @GetMapping("/cancel-booking/{id}")
    public String cancelBooking(@PathVariable("id") Long id) {
        bookingService.cancelBooking(id);
        return "redirect:/booking-history"; // chuyển hướng về trang hiển thị danh sách booking
    }
    @GetMapping("/booking-details/{id}")
    public String bookingDetails(@PathVariable("id") long id,Model model) {
        model.addAttribute("booking", bookingService.getBookingBybid(id));
        return "web/booking-details";
    }

    @GetMapping("/vouchers")
    public String voucher(Model model) {
        model.addAttribute("promotionUpcoming", promotionService.getUpcomingPromotions());
        model.addAttribute("promotionOngoing", promotionService.getOngoingPromotions());
        return "web/voucher";
    }

    @GetMapping({"/host/account", "/customer/account"})
    public String Account(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("user");
        User user = (User) session.getAttribute("user");
        boolean hasHotel = false;

        if (user != null) {
            hasHotel = hotelService.existsByOwnerId(user.getUid());
        }

        model.addAttribute("hasHotel", hasHotel);

        if (loggedInUser == null) {
            return "redirect:/signin";
        }

        Images avatar = imagesRepository.findByOidAndStt(loggedInUser.getUid(), 0);
        model.addAttribute("avatar", avatar);
        model.addAttribute("user", loggedInUser);
        return "web/account";
    }

    @PostMapping("/upload-avatar")
    @ResponseBody
    public Map<String, Object> uploadAvatar(
            @RequestParam("avatar") MultipartFile avatarFile,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Map<String, Object> response = new HashMap<>();

        try {
            User currentUser = (User) session.getAttribute("user");

            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "Bạn chưa đăng nhập.");
                return response;
            }

            // Check if file is empty
            if (avatarFile.isEmpty()) {
                response.put("success", false);
                response.put("message", "Vui lòng chọn ảnh để tải lên.");
                return response;
            }

            // Check file type
            String contentType = avatarFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("success", false);
                response.put("message", "Chỉ chấp nhận file hình ảnh.");
                return response;
            }

            // Upload the image
            String avatarUrl = imageService.uploadImage(avatarFile);

            // Check if user already has an avatar
            Images existingAvatar = imagesRepository.findByOidAndStt(currentUser.getUid(), 0);

            if (existingAvatar != null) {
                // Update existing avatar
                existingAvatar.setImageUrl(avatarUrl);
                imageService.saveImage(existingAvatar);
            } else {
                // Create new avatar entry
                Images newAvatar = new Images();
                newAvatar.setImageUrl(avatarUrl);
                newAvatar.setOid(currentUser.getUid());
                newAvatar.setStt(0); // Use 0 for avatar images
                imageService.saveImage(newAvatar);
            }

            session.setAttribute("avatar", existingAvatar);

            response.put("success", true);
            response.put("message", "Ảnh đại diện đã được cập nhật thành công.");
            response.put("avatarUrl", avatarUrl);

            return response;

        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Lỗi khi tải ảnh lên: " + e.getMessage());
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return response;
        }
    }

    @GetMapping({"/host/edit-account", "/customer/edit-account"})
    public String editAccount(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        boolean hasHotel = false;

        if (user != null) {
            hasHotel = hotelService.existsByOwnerId(user.getUid());
        }

        model.addAttribute("hasHotel", hasHotel);
        User loggedInUser = (User) session.getAttribute("user");
        if (loggedInUser == null) {
            return "redirect:/signin";
        }
        Images avatar = imagesRepository.findByOidAndStt(loggedInUser.getUid(), 0);

        model.addAttribute("user", loggedInUser);
        model.addAttribute("avatar", avatar);
        return "web/edit-account";
    }

    @PostMapping({"/host/update-account", "/customer/update-account"})
    @ResponseBody  // This annotation is required to return JSON
    public Map<String, Object> updateAccount(@RequestParam("uid") Long uid,
                                             @RequestParam("fullname") String fullname,
                                             @RequestParam("email") String email,
                                             @RequestParam("phoneNumber") String phoneNumber,
                                             HttpSession session,
                                             RedirectAttributes redirectAttributes) {
        Map<String, Object> response = new HashMap<>();
        try {
            User currentUser = (User) session.getAttribute("user");

            // Security check that was in your original code but missing in the new version
            if (currentUser == null || !currentUser.getUid().equals(uid)) {
                response.put("success", false);
                response.put("message", "Không có quyền cập nhật thông tin này");
                return response;
            }

            currentUser.setFullname(fullname);
            currentUser.setEmail(email);
            currentUser.setPhoneNumber(phoneNumber);

            User updatedUser = userService.updateUser(currentUser);

            // Update session
            session.setAttribute("user", updatedUser);

            response.put("success", true);
            response.put("message", "Cập nhật thông tin thành công");
            response.put("redirectUrl", "MANAGER".equals(currentUser.getRole()) ?
                    "/host/edit-account" : "/customer/edit-account");

            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return response;
        }
    }
}
