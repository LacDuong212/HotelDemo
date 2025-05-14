package com.example.hotelbookingwebsite.Model;

public class Constants {
    public static final String UNKNOWN = "UNKNOWN";

    // role
    public static class ROLE {
        public static final String CUSTOMER = "CUSTOMER";
        public static final String MANAGER = "MANAGER";
        public static final String ADMIN = "ADMIN";
    }

    // payment method
    public static class PAYMENT_METHOD {
        public static final String VNPAY = "VNPAY";
    }

    // payment status
    public static class PAYMENT_STATUS {
        public static final String PAID = "PAID";
        public static final String REFUNDED = "REFUNDED";
        public static final String UNPAID = "UNPAID";
    }

    // booking status
    public static class BOOKING_STATUS {
        public static final String CANCELLED = "CANCELLED";
        public static final String CHECKED_IN = "CHECKED_IN";
        public static final String CHECKED_OUT = "CHECKED_OUT";
        public static final String CONFIRMED = "CONFIRMED";
        public static final String DENIED = "DENIED";
        public static final String PENDING = "PENDING";
    }

    // room status
    public static class ROOM_STATUS {
        public static final String AVAILABLE = "AVAILABLE";
        public static final String BOOKED = "BOOKED";
        public static final String UNAVAILABLE = "UNAVAILABLE";
    }

    // promotion status
    public static class PROMOTION_STATUS {
        public static final boolean ACTIVE = true;
        public static final boolean INACTIVE = false;
    }
}
