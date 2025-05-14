package com.example.hotelbookingwebsite.Repository;

import com.example.hotelbookingwebsite.Model.Hotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    // Lấy 4 khách sạn mới nhất
    List<Hotel> findTop4ByOrderByCreatedDateDesc();

    // Lấy tất cả khách sạn (không cần điều kiện status)
    List<Hotel> findAllByOrderByCreatedDateDesc();

    @Query(value = "SELECT * FROM hotels h WHERE " +
            "(LOWER(h.address) LIKE LOWER(CONCAT('%', 'Thành phố Hồ Chí Minh', '%')) OR " +
            "LOWER(h.address) LIKE LOWER(CONCAT('%', 'TP.HCM', '%')) OR " +
            "LOWER(h.address) LIKE LOWER(CONCAT('%', 'Hồ Chí Minh', '%'))) " +
            "ORDER BY h.created_date DESC LIMIT 4",
            nativeQuery = true)
    List<Hotel> findTop4NewestHotelsInHCM();

    Page<Hotel> findByAddressContainingIgnoreCase(String address, Pageable pageable);

    boolean existsByManager_Uid(Long uid);

    boolean existsByManagerUid(Long uid);

    Hotel findByManager_Uid(Long uid);
}
