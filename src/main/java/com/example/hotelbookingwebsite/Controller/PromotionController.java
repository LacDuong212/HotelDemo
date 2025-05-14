package com.example.hotelbookingwebsite.Controller;

import com.example.hotelbookingwebsite.Model.Promotion;
import com.example.hotelbookingwebsite.Service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    @GetMapping("/promotion/{code}")
    public ResponseEntity<Map<String, Object>> checkDiscountCode(@PathVariable String code) {
        Promotion promotion = promotionService.findValidPromotionByCode(code);
        if (promotion != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("promotion", promotion.getDiscount());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false));
    }
}
