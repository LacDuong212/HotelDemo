package com.example.hotelbookingwebsite.Service;
import com.example.hotelbookingwebsite.Model.Promotion;
import com.example.hotelbookingwebsite.Repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
public class PromotionService {
    private final PromotionRepository promotionRepository;
    @Autowired
    public PromotionService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }
    public List<Promotion> getUpcomingPromotions() {
        return promotionRepository.findByStartDateAfter(LocalDate.now());
    }

    public List<Promotion> getOngoingPromotions() {
        LocalDate today = LocalDate.now();
        return promotionRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(today, today);
    }

	public List<Promotion> getValidPromotions() {
        LocalDate now = LocalDate.now();
        return promotionRepository.findAll().stream()
                .filter(p -> p.isStatus()
                        && p.getStartDate() != null
                        && p.getEndDate() != null
                        && !now.isAfter(p.getEndDate()))
                .collect(Collectors.toList());
    }

    public List<Promotion> getExpiredPromotions() {
        LocalDate now = LocalDate.now();
        return promotionRepository.findAll().stream()
                .filter(p -> !p.isStatus()
                        || now.isAfter(p.getEndDate()))
                .collect(Collectors.toList());
    }

    public Promotion findValidPromotionByCode(String code) {
        Optional<Promotion> optionalPromotion = promotionRepository.findByCode(code);

        if (optionalPromotion.isPresent()) {
            Promotion promotion = optionalPromotion.get();
            LocalDate now = LocalDate.now();

            boolean isValid = promotion.isStatus()
                    && promotion.getStartDate() != null
                    && promotion.getEndDate() != null
                    && !now.isBefore(promotion.getStartDate())
                    && !now.isAfter(promotion.getEndDate());

            return isValid ? promotion : null;
        }

        return null;
    }
}