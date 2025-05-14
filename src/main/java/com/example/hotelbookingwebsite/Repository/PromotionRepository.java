package com.example.hotelbookingwebsite.Repository;

import com.example.hotelbookingwebsite.Model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    List<Promotion> findByStartDateAfter(LocalDate today); // Sắp tới
    List<Promotion> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate start, LocalDate end); // Đang diễn ra
    Optional<Promotion> findByCode(String code);
}
