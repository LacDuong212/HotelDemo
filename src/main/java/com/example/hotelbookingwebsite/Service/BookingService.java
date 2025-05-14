package com.example.hotelbookingwebsite.Service;

import com.example.hotelbookingwebsite.DTO.BookingDTO;
import com.example.hotelbookingwebsite.Model.*;
import com.example.hotelbookingwebsite.Repository.BookingRepository;
import com.example.hotelbookingwebsite.Repository.CustomerRepository;
import com.example.hotelbookingwebsite.Repository.PaymentRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final PaymentRepository paymentRepository;
    private final RoomService roomService;

    @Autowired
    private HttpSession session;

    @Autowired
    public BookingService(BookingRepository bookingRepository, CustomerRepository customerRepository,
                          PaymentRepository paymentRepository, RoomService roomService) {
        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
        this.paymentRepository = paymentRepository;
        this.roomService = roomService;
    }
    public BookingDTO getBookingBybid(long bid) {
        return convertToDTO(bookingRepository.findById(bid).get());
    }
    public List<BookingDTO> getBookingByUidAndStatus(long uid, String status) {
        Customer customer = customerRepository.findById(uid)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + uid));

        List<Booking> bookings = bookingRepository.findByCustomerAndStatus(customer, status);

        return bookings.stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<BookingDTO> getUpcomingBookings(long uid) {
        Customer customer = customerRepository.findById(uid)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + uid));

        List<String> statuses = List.of("UNPAID", "PAID");

        List<Booking> bookings = bookingRepository.findByCustomerAndStatusInAndCheckInDateAfter(customer, statuses, LocalDate.now());

        return bookings.stream().map(this::convertToDTO).toList();
    }

    public List<BookingDTO> getPaidBookings(long uid) {
        Customer customer = customerRepository.findById(uid)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + uid));
        List<Booking> bookings = bookingRepository.findByCustomerAndStatusAndCheckInDateLessThanEqual(customer, "PAID", LocalDate.now());
        return bookings.stream().map(this::convertToDTO).toList();
    }

    private BookingDTO convertToDTO(Booking booking) {
        BookingDTO dto = new BookingDTO();
        dto.setBid(booking.getBid());
        dto.setCheckInDate(booking.getCheckInDate());
        dto.setCheckOutDate(booking.getCheckOutDate());
        dto.setStatus(booking.getStatus());
        dto.setTotalPrice(booking.getTotalPrice());
        dto.setCustomer(booking.getCustomer());
        dto.setPayment(booking.getPayment());
        dto.setRoom(roomService.convertToRoomDTO(booking.getRoom()));
        return dto;
    }
    public void cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking"));

        booking.setStatus(Constants.PAYMENT_STATUS.REFUNDED); // đổi trạng thái
        bookingRepository.save(booking);
    }
    public Long createPendingBooking(float totalPrice, String checkin, String checkout, Long roomId) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate checkInDate = LocalDate.parse(checkin, formatter);
            LocalDate checkOutDate = LocalDate.parse(checkout, formatter);

            User loggedInUser = (User) session.getAttribute("user");
            if (loggedInUser == null) {
                throw new RuntimeException("User not logged in");
            }

            Booking booking = new Booking();
            booking.setCheckInDate(checkInDate);
            booking.setCheckOutDate(checkOutDate);
            booking.setStatus(Constants.PAYMENT_STATUS.UNPAID);
            booking.setTotalPrice(totalPrice);
            // Tìm phòng theo roomId
            Room room = roomService.getRoomById(roomId); // Bạn cần tạo phương thức getRoomById trong RoomService
            booking.setRoom(room);

            // Tìm customer theo uid
            Customer customer = customerRepository.findById(loggedInUser.getUid())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy customer với ID: " + loggedInUser.getUid()));
            booking.setCustomer(customer);  // Gán customer vào booking

            // Tạo đối tượng Payment và gán vào booking
            Payment payment = new Payment();
            payment.setPaymentMethod(Constants.PAYMENT_METHOD.VNPAY);
            payment.setStatus(Constants.PAYMENT_STATUS.UNPAID);  // Bạn có thể tùy chỉnh trạng thái thanh toán tại đây
            paymentRepository.save(payment);  // Lưu bản ghi payment vào cơ sở dữ liệu
            booking.setPayment(payment);  // Gán payment vào booking

            bookingRepository.save(booking);

            return booking.getBid();
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Invalid date format for check-in or check-out.", e);
        }
    }

    public void markBookingAsPaid(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        booking.setStatus("PAID");
        bookingRepository.save(booking);
    }
}
