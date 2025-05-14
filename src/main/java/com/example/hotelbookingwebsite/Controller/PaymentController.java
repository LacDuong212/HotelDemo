package com.example.hotelbookingwebsite.Controller;

import com.example.hotelbookingwebsite.Service.BookingService;
import com.example.hotelbookingwebsite.Service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PaymentController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/payment-confirm")
    public String processPayment(@RequestParam("checkin") String checkin,
                                 @RequestParam("checkout") String checkout,
                                 @RequestParam("totalPrice") Float totalAmount,
                                 @RequestParam("roomId") Long roomId,
                                 HttpServletRequest request) {

        Long bookingId = bookingService.createPendingBooking(totalAmount, checkin, checkout, roomId); // Cần viết hàm này
        request.getSession().setAttribute("pendingBookingId", bookingId);

        int totalAmountInCents = (int) (totalAmount * 100);

        String orderInfo = "Thanh toan dat phong";

        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String vnpayUrl = paymentService.createOrder(totalAmountInCents, orderInfo, baseUrl, request);

        return "redirect:" + vnpayUrl;
    }

    @GetMapping("/repayment")
    public String processRePayment(@RequestParam("bookingId") Long bookingId,
                                 @RequestParam("totalPrice") Float totalAmount,
                                 HttpServletRequest request) {

        int totalAmountInCents = (int) (totalAmount * 100);

        String orderInfo = "Thanh toan dat phong";

        // Lưu bookingId vào session để sử dụng ở vnpayReturn
        request.getSession().setAttribute("pendingBookingId", bookingId);

        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String vnpayUrl = paymentService.createOrder(totalAmountInCents, orderInfo, baseUrl, request);

        return "redirect:" + vnpayUrl;
    }

    @GetMapping("/vnpay-payment")
    public String vnpayReturn(HttpServletRequest request, Model model) {
        int paymentStatus = paymentService.orderReturn(request);
        Long bookingId = (Long) request.getSession().getAttribute("pendingBookingId");

        if (paymentStatus == 1 && bookingId != null) {
            // Bước 1: Cập nhật trạng thái booking
            bookingService.markBookingAsPaid(bookingId);

            // Bước 2: Tạo bản ghi payment
            paymentService.savePaymentInfo(bookingId, request);

            model.addAttribute("bookingId", bookingId);
            return "web/payment-success"; // trang thông báo thanh toán thành công
        } else {
            model.addAttribute("bookingId", bookingId);
            return "web/payment-failed"; // trang thông báo thanh toán thất bại
        }
    }
}
