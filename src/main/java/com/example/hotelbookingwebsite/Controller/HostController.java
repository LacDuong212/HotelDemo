package com.example.hotelbookingwebsite.Controller;

import com.example.hotelbookingwebsite.DTO.BookedRoomDTO;
import com.example.hotelbookingwebsite.DTO.HotelDTO;
import com.example.hotelbookingwebsite.DTO.RoomDTO;
import com.example.hotelbookingwebsite.Model.*;
import com.example.hotelbookingwebsite.Repository.*;
import com.example.hotelbookingwebsite.Service.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/host")
public class HostController {

    @Autowired
    private HotelService hotelService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private ImagesRepository imagesRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping("/add-hotel")
    public String showAddHotelForm(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        boolean hasHotel = false;

        if (user != null) {
            hasHotel = hotelService.existsByOwnerId(user.getUid());
        }

        model.addAttribute("hasHotel", hasHotel);
        model.addAttribute("hotelDTO", new HotelDTO());
        return "host/add-hotel";
    }

    @PostMapping("/add-hotel")
    public String addHotel(
            @Valid @ModelAttribute("hotelDTO") HotelDTO hotelDTO,
            HttpSession session,
            BindingResult result,
            @RequestParam("mainImage") MultipartFile mainImage,
            @RequestParam(value = "additionalImages", required = false) MultipartFile[] additionalImages,
            @RequestParam(value = "extraImages", required = false) MultipartFile[] extraImages,
            RedirectAttributes redirectAttributes
    ) {
        User loggedInUser = (User) session.getAttribute("user");
        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn chưa đăng nhập.");
            return "redirect:/signin";
        }

        if (result.hasErrors()) {
            return "host/add-hotel";
        }

        try {
            Long hotelId = hotelService.saveHotel(hotelDTO, loggedInUser.getUid());

            // Xử lý ảnh chính
            if (!mainImage.isEmpty()) {
                String mainImageUrl = imageService.uploadImage(mainImage);
                Images mainImageEntity = new Images();
                mainImageEntity.setImageUrl(mainImageUrl);
                mainImageEntity.setOid(hotelId);
                mainImageEntity.setStt(0);
                imageService.saveImage(mainImageEntity);
            }

            // Xử lý các ảnh thumbnail cơ bản
            List<MultipartFile> allAdditionalImages = new ArrayList<>();

            // Thêm ảnh từ additionalImages nếu có
            if (additionalImages != null) {
                for (MultipartFile file : additionalImages) {
                    if (file != null && !file.isEmpty()) {
                        allAdditionalImages.add(file);
                    }
                }
            }

            // Thêm ảnh từ extraImages nếu có
            if (extraImages != null) {
                for (MultipartFile file : extraImages) {
                    if (file != null && !file.isEmpty()) {
                        allAdditionalImages.add(file);
                    }
                }
            }

            // Lưu tất cả các ảnh bổ sung
            int order = 1;
            for (MultipartFile file : allAdditionalImages) {
                String imageUrl = imageService.uploadImage(file);
                Images imageEntity = new Images();
                imageEntity.setImageUrl(imageUrl);
                imageEntity.setOid(hotelId);
                imageEntity.setStt(order++);
                imageService.saveImage(imageEntity);
            }

            redirectAttributes.addFlashAttribute("success", "Khách sạn đã được thêm thành công!");
            return "redirect:/host/info-hotel";

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tải ảnh lên: " + e.getMessage());
            return "redirect:/host/add-hotel";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi thêm khách sạn: " + e.getMessage());
            return "redirect:/host/add-hotel";
        }
    }

    @GetMapping("/info-hotel")
    public String InfoHotel(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("user");

        if (loggedInUser == null) {
            return "redirect:/signin";
        }

        Hotel hotel = hotelService.getHotelByHostUid(loggedInUser.getUid());
        List<Images> imagesList = imagesRepository.findByOidOrderBySttAsc(hotel.getHid());

        session.setAttribute("hotel", hotel);
        model.addAttribute("hotel", hotel);
        session.setAttribute("imagesList", imagesList);
        model.addAttribute("imagesList", imagesList);

        model.addAttribute("hasHotel", true); // thêm dòng này

        return "host/info-hotel";
    }

    @GetMapping("/edit-info-hotel/{id}")
    public String editInfoHotel(@PathVariable("id") Long id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        boolean hasHotel = false;

        if (user != null) {
            hasHotel = hotelService.existsByOwnerId(user.getUid());
        }

        model.addAttribute("hasHotel", hasHotel);
        Hotel hotel = hotelService.getHotelById(id);
        if (hotel == null) {
            model.addAttribute("error", "Không tìm thấy thông tin khách sạn.");
            return "redirect:/host/add-hotel";
        }

        List<Images> imagesList = imagesRepository.findByOidOrderBySttAsc(hotel.getHid());

        model.addAttribute("hotel", hotel);
        session.setAttribute("hotel", hotel);
        model.addAttribute("imagesList", imagesList);
        session.setAttribute("imagesList", imagesList);
        return "host/edit-info-hotel";
    }

    @PostMapping("/update-hotel")
    public String updateHotel(
            @RequestParam("hotelId") Long hotelId,
            @RequestParam("hotelName") String hotelName,
            @RequestParam("hotelAddress") String hotelAddress,
            @RequestParam("hotelDescription") String hotelDescription,
            @RequestParam(value = "mainImage", required = false) MultipartFile mainImage,
            @RequestParam(value = "thumbnailImage0", required = false) MultipartFile thumbnailImage0,
            @RequestParam(value = "thumbnailImage1", required = false) MultipartFile thumbnailImage1,
            @RequestParam(value = "thumbnailImage2", required = false) MultipartFile thumbnailImage2,
            @RequestParam(value = "extraImages", required = false) MultipartFile[] extraImages,
            @RequestParam(value = "additionalImages", required = false) MultipartFile[] additionalImages,
            RedirectAttributes redirectAttributes,
            HttpSession session,
            Model model
    ) {
        User loggedInUser = (User) session.getAttribute("user");
        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn chưa đăng nhập.");
            return "redirect:/signin";
        }

        try {
            Hotel hotel = hotelService.getHotelById(hotelId);
            hotel.setName(hotelName);
            hotel.setAddress(hotelAddress);
            hotel.setDescription(hotelDescription);
            hotelService.updateHotel(hotel);

            boolean imagesUpdated = false;

            // Process main image
            if (mainImage != null && !mainImage.isEmpty()) {
                Images mainImageEntity = imagesRepository.findByOidAndStt(hotelId, 0);
                if (mainImageEntity != null) {
                    String newImageUrl = imageService.uploadImage(mainImage);
                    mainImageEntity.setImageUrl(newImageUrl);
                    imageService.saveImage(mainImageEntity);
                } else {
                    String newImageUrl = imageService.uploadImage(mainImage);
                    Images newMainImage = new Images();
                    newMainImage.setOid(hotelId);
                    newMainImage.setStt(0);
                    newMainImage.setImageUrl(newImageUrl);
                    imageService.saveImage(newMainImage);
                }
                imagesUpdated = true;
            }

            // Process individual thumbnails (for updating existing thumbnail images)
            if (thumbnailImage0 != null && !thumbnailImage0.isEmpty()) {
                updateOrCreateImage(thumbnailImage0, hotelId, 1);
                imagesUpdated = true;
            }

            if (thumbnailImage1 != null && !thumbnailImage1.isEmpty()) {
                updateOrCreateImage(thumbnailImage1, hotelId, 2);
                imagesUpdated = true;
            }

            if (thumbnailImage2 != null && !thumbnailImage2.isEmpty()) {
                updateOrCreateImage(thumbnailImage2, hotelId, 3);
                imagesUpdated = true;
            }

            // Collect all additional images from different sources
            List<MultipartFile> allAdditionalImages = new ArrayList<>();

            // Add images from extraImages (for new thumbnails in empty slots)
            if (extraImages != null) {
                for (MultipartFile file : extraImages) {
                    if (file != null && !file.isEmpty()) {
                        allAdditionalImages.add(file);
                        imagesUpdated = true;
                    }
                }
            }

            // Add images from additionalImages (for "Thêm ảnh" button)
            if (additionalImages != null) {
                for (MultipartFile file : additionalImages) {
                    if (file != null && !file.isEmpty()) {
                        allAdditionalImages.add(file);
                        imagesUpdated = true;
                    }
                }
            }

            // Save all additional images
            if (!allAdditionalImages.isEmpty()) {
                // Get the maximum STT value for this hotel
                Integer maxStt = imagesRepository.findMaxSttByOid(hotelId);
                int nextStt = (maxStt != null) ? maxStt + 1 : 1;

                for (MultipartFile file : allAdditionalImages) {
                    String imageUrl = imageService.uploadImage(file);
                    Images imageEntity = new Images();
                    imageEntity.setImageUrl(imageUrl);
                    imageEntity.setOid(hotelId);
                    imageEntity.setStt(nextStt++);
                    imageService.saveImage(imageEntity);
                }
            }

            // Refresh the image list if any images were updated
            if (imagesUpdated) {
                List<Images> updatedImagesList = imagesRepository.findByOidOrderBySttAsc(hotelId);
                session.setAttribute("imagesList", updatedImagesList);
                model.addAttribute("imagesList", updatedImagesList);
            }

            redirectAttributes.addFlashAttribute("success", "Cập nhật khách sạn thành công!");
            return "redirect:/host/info-hotel";

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tải ảnh lên: " + e.getMessage());
            return "redirect:/host/edit-info-hotel/" + hotelId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật khách sạn: " + e.getMessage());
            return "redirect:/host/edit-info-hotel/" + hotelId;
        }
    }

    // Helper method to update an existing image or create a new one at a specific position
    private void updateOrCreateImage(MultipartFile imageFile, Long hotelId, int position) throws IOException {
        if (imageFile == null || imageFile.isEmpty()) {
            return;
        }

        // Check if an image exists at this position
        Images existingImage = imagesRepository.findByOidAndStt(hotelId, position);
        String newImageUrl = imageService.uploadImage(imageFile);

        if (existingImage != null) {
            // Update existing image
            existingImage.setImageUrl(newImageUrl);
            imageService.saveImage(existingImage);
        } else {
            // Create new image at this position
            Images newImage = new Images();
            newImage.setOid(hotelId);
            newImage.setStt(position);
            newImage.setImageUrl(newImageUrl);
            imageService.saveImage(newImage);
        }
    }

    // New method to show confirmation page before deleting an image
    @GetMapping("/remove-image-confirm")
    public String confirmRemoveImage(@RequestParam("id") Long imageId, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("user");
        if (loggedInUser == null) {
            return "redirect:/signin";
        }

        try {
            Images image = imagesRepository.findById(imageId).orElse(null);
            if (image == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy hình ảnh.");
                return "redirect:/host/info-hotel";
            }

            // Don't allow deleting main image (STT = 0)
            if (image.getStt() == 0) {
                redirectAttributes.addFlashAttribute("error", "Không thể xóa ảnh chính của khách sạn.");
                return "redirect:/host/edit-info-hotel/" + image.getOid();
            }

            // Get hotel for the return path
            Hotel hotel = hotelService.getHotelById(image.getOid());
            if (hotel == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin khách sạn.");
                return "redirect:/host/add-hotel";
            }

            // Set up confirmation data
            model.addAttribute("showConfirmation", true);
            model.addAttribute("imageToDelete", imageId);
            model.addAttribute("hotel", hotel);

            // Get all images for the hotel to display on the page
            List<Images> imagesList = imagesRepository.findByOidOrderBySttAsc(hotel.getHid());
            model.addAttribute("imagesList", imagesList);

            return "host/edit-info-hotel";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xử lý yêu cầu: " + e.getMessage());
            return "redirect:/host/info-hotel";
        }
    }

    // Add this method to actually perform the deletion
    @GetMapping("/delete-image")
    public String deleteImage(@RequestParam("id") Long imageId, RedirectAttributes redirectAttributes, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("user");
        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn chưa đăng nhập.");
            return "redirect:/signin";
        }

        try {
            Images image = imagesRepository.findById(imageId).orElse(null);
            if (image == null ) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy hình ảnh.");
                return "redirect:/host/info-hotel";
            }

            // Don't allow deleting main image (STT = 0)
            if (image.getStt() == 0) {
                redirectAttributes.addFlashAttribute("error", "Không thể xóa ảnh chính của khách sạn.");
                return "redirect:/host/edit-info-hotel/" + image.getOid();
            }

            Long hotelId = image.getOid();

            // Delete the image
            imagesRepository.deleteById(imageId);

            // Reindex the STT values to maintain continuity
            List<Images> remainingImages = imagesRepository.findByOidOrderBySttAsc(hotelId);
            for (int i = 0; i < remainingImages.size(); i++) {
                Images img = remainingImages.get(i);
                // Skip the main image (STT = 0)
                if (img.getStt() > 0) {
                    img.setStt(i); // Adjust index as needed
                    imageService.saveImage(img);
                }
            }

            // Update the image list in session
            List<Images> updatedImagesList = imagesRepository.findByOidOrderBySttAsc(hotelId);
            session.setAttribute("imagesList", updatedImagesList);

            redirectAttributes.addFlashAttribute("success", "Xóa ảnh thành công!");

            return "redirect:/host/info-hotel";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa ảnh: " + e.getMessage());
            return "redirect:/host/info-hotel";
        }
    }

    @GetMapping("/delete-image-room")
    public String deleteImageRoom(@RequestParam("id") Long imageId, @RequestParam("imageIndex") int imageIndex,
                                  RedirectAttributes redirectAttributes, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("user");
        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn chưa đăng nhập.");
            return "redirect:/signin";
        }

        try {
            List<Long> image = imagesRepository.findIidByOid(imageId);
            if (image == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy hình ảnh.");
                return "redirect:/host/room/edit/" + imageId.toString();
            }

            // Don't allow deleting main image (STT = 0)
            if (image.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không thể xóa ảnh chính của khách sạn.");
                return "redirect:/host/room/edit/" + imageId.toString();
            }

            Long imageRoomId = image.get(imageIndex);

            // Delete the image
            imagesRepository.deleteById(imageRoomId);

            redirectAttributes.addFlashAttribute("success", "Xóa ảnh thành công!");

            return "redirect:/host/room/edit/" + imageId.toString();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa ảnh: " + e.getMessage());
            return "redirect:/host/room/edit/" + imageId.toString();
        }
    }

    @GetMapping("/add-room")
    public String showAddRoomForm(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        boolean hasHotel = false;

        if (user != null) {
            hasHotel = hotelService.existsByOwnerId(user.getUid());
        }

        model.addAttribute("hasHotel", hasHotel);
        model.addAttribute("roomDTO", new RoomDTO());
        return "host/add-room";  // Trả về trang thêm phòng
    }

    @PostMapping("/add-room")
    public String addRoom(
            @Valid @ModelAttribute("roomDTO") RoomDTO roomDTO,
            HttpSession session,
            BindingResult result,
            @RequestParam("mainImage") MultipartFile mainImage,
            @RequestParam(value = "additionalImages", required = false) MultipartFile[] additionalImages,
            @RequestParam(value = "extraImages", required = false) MultipartFile[] extraImages,
            Model model
    ) {
        // Kiểm tra xem người dùng đã đăng nhập chưa
        User loggedInUser = (User) session.getAttribute("user");
        if (loggedInUser == null) {
            model.addAttribute("error", "Bạn chưa đăng nhập.");
            return "host/add-room";  // Trả về cùng trang và hiển thị thông báo lỗi
        }

        // Kiểm tra xem có lỗi trong form không
        if (result.hasErrors()) {
            return "host/add-room";
        }

        try {
            // Lưu phòng vào cơ sở dữ liệu và lấy ID phòng
            Long roomId = roomService.saveRoom(roomDTO, loggedInUser.getUid());

            // Xử lý ảnh chính (main image)
            if (!mainImage.isEmpty()) {
                String mainImageUrl = imageService.uploadImage(mainImage);
                Images mainImageEntity = new Images();
                mainImageEntity.setImageUrl(mainImageUrl);
                mainImageEntity.setOid(roomId);  // Gán oid là ID của phòng
                mainImageEntity.setStt(0);  // Đặt thứ tự ảnh là 0 cho ảnh chính
                imageService.saveImage(mainImageEntity);
            }

            // Xử lý các ảnh bổ sung (additional images)
            List<MultipartFile> allAdditionalImages = new ArrayList<>();
            if (additionalImages != null) {
                for (MultipartFile file : additionalImages) {
                    if (file != null && !file.isEmpty()) {
                        allAdditionalImages.add(file);
                    }
                }
            }

            // Xử lý các ảnh extra images
            if (extraImages != null) {
                for (MultipartFile file : extraImages) {
                    if (file != null && !file.isEmpty()) {
                        allAdditionalImages.add(file);
                    }
                }
            }

            // Lưu tất cả các ảnh bổ sung vào cơ sở dữ liệu
            int order = 1;
            for (MultipartFile file : allAdditionalImages) {
                String imageUrl = imageService.uploadImage(file);
                Images imageEntity = new Images();
                imageEntity.setImageUrl(imageUrl);
                imageEntity.setOid(roomId);  // Gán oid là ID của phòng
                imageEntity.setStt(order++);  // Tăng thứ tự cho từng ảnh
                imageService.saveImage(imageEntity);
            }

            Hotel hotel = hotelService.getHotelByHostUid(loggedInUser.getUid());

            // Lấy tất cả các phòng của khách sạn
            List<RoomDTO> rooms = roomService.getAllRoomByHotel(hotel);

            if (rooms.isEmpty()) {
                model.addAttribute("message", "Khách sạn của bạn chưa có phòng nào.");
            }

            model.addAttribute("rooms", rooms);
            model.addAttribute("success", "Phòng đã được thêm thành công!");
            return "redirect:/host/list-room";   // Chuyển hướng tới danh sách phòng và hiển thị thông báo thành công

        } catch (IOException e) {
            model.addAttribute("error", "Lỗi khi tải ảnh lên: " + e.getMessage());
            return "redirect:/host/add-room";  // Quay lại trang thêm phòng nếu có lỗi
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi thêm phòng: " + e.getMessage());
            return "redirect:/host/add-room";  // Quay lại trang nếu có lỗi
        }
    }

    @GetMapping("/list-book-room")
    public String listBookedRooms(Model model, HttpSession session) {
        // Lấy thông tin người quản lý từ session
        User loggedInUser = (User) session.getAttribute("user");
        boolean hasHotel = false;

        if (loggedInUser != null) {
            hasHotel = hotelService.existsByOwnerId(loggedInUser.getUid());
        }

        model.addAttribute("hasHotel", hasHotel);

        if (loggedInUser == null) {
            model.addAttribute("error", "Bạn chưa đăng nhập.");
            return "redirect:/signin";  // Điều hướng đến trang đăng nhập nếu chưa đăng nhập
        }

        // Lấy khách sạn của người quản lý
        Hotel hotel = hotelService.getHotelByHostUid(loggedInUser.getUid());

        // Lấy tất cả các phòng của khách sạn
        List<RoomDTO> allRooms = roomService.getAllRoomByHotel(hotel);

        // Lọc ra các phòng đã được đặt
        List<RoomDTO> bookedRooms = allRooms.stream()
                .filter(roomDTO -> Objects.equals(roomDTO.getStatus(), "AVAILABLE"))
                .collect(Collectors.toList());

        LocalDate today = LocalDate.now();

        List<BookedRoomDTO> upcomingBookings = new ArrayList<>();
        List<BookedRoomDTO> pastBookings = new ArrayList<>();

        for (RoomDTO roomDTO : bookedRooms) {
            Long roomId = roomDTO.getRid();
            List<Booking> bookings = bookingRepository.findAllByRoom_Rid(roomId);

            for (Booking booking : bookings) {
                BookedRoomDTO dto = new BookedRoomDTO(
                        roomDTO,
                        booking.getCheckInDate(),
                        booking.getCheckOutDate(),
                        booking.getCustomer().getUser().getFullname(),
                        booking.getStatus().toUpperCase()
                );

                if (!booking.getCheckOutDate().isBefore(today)) {
                    upcomingBookings.add(dto);
                } else {
                    pastBookings.add(dto);
                }
            }
        }

        if (bookedRooms.isEmpty()) {
            model.addAttribute("message", "Không có phòng nào đã được đặt.");
        }

        List<BookedRoomDTO> cancelledBookings = new ArrayList<>();
        cancelledBookings.addAll(
                upcomingBookings.stream()
                        .filter(b -> "REFUNDED".equalsIgnoreCase(b.getStatus()))
                        .collect(Collectors.toList())
        );
        cancelledBookings.addAll(
                pastBookings.stream()
                        .filter(b -> "REFUNDED".equalsIgnoreCase(b.getStatus()))
                        .collect(Collectors.toList())
        );

        model.addAttribute("cancelledBookings", cancelledBookings);
        model.addAttribute("upcomingBookings", upcomingBookings);
        model.addAttribute("pastBookings", pastBookings);

        return "host/list-book-room";  // Chuyển tới trang hiển thị danh sách phòng đã được đặt
    }

    @GetMapping("/list-room")
    public String listRooms(Model model, HttpSession session) {
        // Lấy thông tin người quản lý từ session
        User loggedInUser = (User) session.getAttribute("user");
        User user = (User) session.getAttribute("user");
        boolean hasHotel = false;

        if (user != null) {
            hasHotel = hotelService.existsByOwnerId(user.getUid());
        }

        model.addAttribute("hasHotel", hasHotel);

        if (loggedInUser == null) {
            model.addAttribute("error", "Bạn chưa đăng nhập.");
            return "redirect:/signin";  // Điều hướng đến trang đăng nhập nếu chưa đăng nhập
        }

        // Lấy khách sạn của người quản lý
        Hotel hotel = hotelService.getHotelByHostUid(loggedInUser.getUid());

        // Lấy tất cả các phòng của khách sạn
        List<RoomDTO> rooms = roomService.getAllRoomByHotel(hotel);

        if (rooms.isEmpty()) {
            model.addAttribute("message", "Khách sạn của bạn chưa có phòng nào.");
        }

        model.addAttribute("rooms", rooms);
        return "host/list-room";  // Chuyển tới trang hiển thị danh sách phòng
    }

    @GetMapping("/room/edit/{id}")
    public String showEditRoomForm(@PathVariable("id") Long id, Model model) {
        /*Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng"));*/

        RoomDTO rooms = roomService.getRoomDTOById(id);
        model.addAttribute("room", rooms);
        return "host/edit-info-room";
    }

    @PostMapping("/room/edit")
    public String updateRoom(
            @RequestParam("roomId") Long roomId,
            @RequestParam("roomName") String roomName,
            @RequestParam("roomPrice") String roomPrice,
            @RequestParam("roomDescription") String roomDescription,
            @RequestParam(value = "mainImage", required = false) MultipartFile mainImage,
            @RequestParam(value = "thumbnailImage0", required = false) MultipartFile thumbnailImage0,
            @RequestParam(value = "thumbnailImage1", required = false) MultipartFile thumbnailImage1,
            @RequestParam(value = "thumbnailImage2", required = false) MultipartFile thumbnailImage2,
            @RequestParam(value = "extraImages", required = false) MultipartFile[] extraImages,
            @RequestParam(value = "additionalImages", required = false) MultipartFile[] additionalImages,
            RedirectAttributes redirectAttributes,
            HttpSession session,
            Model model
    ) {
        User loggedInUser = (User) session.getAttribute("user");
        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn chưa đăng nhập.");
            return "redirect:/signin";
        }

        try {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng"));

            // Validate if this room belongs to the logged-in user's hotel
            Hotel hotel = hotelService.getHotelByHostUid(loggedInUser.getUid());
            if (hotel == null || !room.getHotel().getHid().equals(hotel.getHid())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền chỉnh sửa phòng này.");
                return "redirect:/host/list-room";
            }

            // Update room details
            room.setRoomName(roomName);

            // Parse price string to double, handle format with commas if necessary
            try {
                String priceStr = roomPrice.replaceAll("\\.", "");
                System.out.println(priceStr);
                float price = Float.parseFloat(priceStr);
                room.setPrice(price);
            } catch (NumberFormatException e) {
                redirectAttributes.addFlashAttribute("error", "Định dạng giá phòng không hợp lệ.");
                return "redirect:/host/room/edit/" + roomId.toString();
            }

            room.setDescription(roomDescription);
            roomRepository.save(room);

            boolean imagesUpdated = false;

            // Process main image
            if (mainImage != null && !mainImage.isEmpty()) {
                Images mainImageEntity = imagesRepository.findByOidAndStt(roomId, 0);
                if (mainImageEntity != null) {
                    String newImageUrl = imageService.uploadImage(mainImage);
                    mainImageEntity.setImageUrl(newImageUrl);
                    imageService.saveImage(mainImageEntity);
                } else {
                    String newImageUrl = imageService.uploadImage(mainImage);
                    Images newMainImage = new Images();
                    newMainImage.setOid(roomId);
                    newMainImage.setStt(0);
                    newMainImage.setImageUrl(newImageUrl);
                    imageService.saveImage(newMainImage);
                }
                imagesUpdated = true;
            }

            // Process individual thumbnails (for updating existing thumbnail images)
            if (thumbnailImage0 != null && !thumbnailImage0.isEmpty()) {
                updateOrCreateImage(thumbnailImage0, roomId, 1);
                imagesUpdated = true;
            }

            if (thumbnailImage1 != null && !thumbnailImage1.isEmpty()) {
                updateOrCreateImage(thumbnailImage1, roomId, 2);
                imagesUpdated = true;
            }

            if (thumbnailImage2 != null && !thumbnailImage2.isEmpty()) {
                updateOrCreateImage(thumbnailImage2, roomId, 3);
                imagesUpdated = true;
            }

            // Collect all additional images from different sources
            List<MultipartFile> allAdditionalImages = new ArrayList<>();

            // Add images from extraImages (for new thumbnails in empty slots)
            if (extraImages != null) {
                for (MultipartFile file : extraImages) {
                    if (file != null && !file.isEmpty()) {
                        allAdditionalImages.add(file);
                        imagesUpdated = true;
                    }
                }
            }

            // Add images from additionalImages (for "Thêm ảnh" button)
            if (additionalImages != null) {
                for (MultipartFile file : additionalImages) {
                    if (file != null && !file.isEmpty()) {
                        allAdditionalImages.add(file);
                        imagesUpdated = true;
                    }
                }
            }

            // Save all additional images
            if (!allAdditionalImages.isEmpty()) {
                // Get the maximum STT value for this room
                Integer maxStt = imagesRepository.findMaxSttByOid(roomId);
                int nextStt = (maxStt != null) ? maxStt + 1 : 1;

                for (MultipartFile file : allAdditionalImages) {
                    String imageUrl = imageService.uploadImage(file);
                    Images imageEntity = new Images();
                    imageEntity.setImageUrl(imageUrl);
                    imageEntity.setOid(roomId);
                    imageEntity.setStt(nextStt++);
                    imageService.saveImage(imageEntity);
                }
            }

            // Refresh the image list if any images were updated
            if (imagesUpdated) {
                List<Images> updatedImagesList = imagesRepository.findByOidOrderBySttAsc(roomId);
                session.setAttribute("imagesList", updatedImagesList);
                model.addAttribute("imagesList", updatedImagesList);
            }

            redirectAttributes.addFlashAttribute("success", "Cập nhật phòng thành công!");
            return "redirect:/host/room/edit/" + roomId.toString();

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tải ảnh lên: " + e.getMessage());
            return "redirect:/host/room/edit/" + roomId.toString();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật phòng: " + e.getMessage());
            return "redirect:/host/room/edit/" + roomId.toString();
        }
    }

    @DeleteMapping("/delete-room/{rid}")
    @ResponseBody
    public Map<String, Object> deleteRoom(@PathVariable("rid") Long rid, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Kiểm tra xem người dùng đã đăng nhập chưa
            User loggedInUser = (User) session.getAttribute("user");
            if (loggedInUser == null) {
                response.put("success", false);
                response.put("message", "Bạn chưa đăng nhập");
                return response;
            }

            // Kiểm tra xem phòng có tồn tại không và thuộc về host hiện tại không
            Room room = roomService.getRoomById(rid);
            if (room == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy phòng");
                return response;
            }

            // Kiểm tra quyền sở hữu phòng (phòng phải thuộc về hotel của host)
            Hotel hotel = hotelService.getHotelByHostUid(loggedInUser.getUid());
            if (hotel == null || !room.getHotel().getHid().equals(hotel.getHid())) {
                response.put("success", false);
                response.put("message", "Bạn không có quyền xóa phòng này");
                return response;
            }

            // Xóa phòng
            roomRepository.deleteById(rid);
            imagesRepository.deleteAllByOid(rid);

            response.put("success", true);
            response.put("message", "Phòng đã được xóa thành công");
        } catch (Exception e) {
            /*response.put("success", false);
            response.put("message", "Lỗi khi xóa phòng: " + e.getMessage());*/
        }

        return response;
    }
}
