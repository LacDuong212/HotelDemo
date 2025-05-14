package com.example.hotelbookingwebsite.Repository;

import com.example.hotelbookingwebsite.Model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    void deleteAllByHotel_Hid(Long hotel_hid);
}
