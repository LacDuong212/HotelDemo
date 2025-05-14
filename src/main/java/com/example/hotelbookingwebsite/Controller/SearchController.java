package com.example.hotelbookingwebsite.Controller;

import com.example.hotelbookingwebsite.DTO.HotelDTO;
import com.example.hotelbookingwebsite.Service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/search")
public class SearchController {

    private final HotelService hotelService;

    @Autowired
    public SearchController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping
    public String searchHotels(
            @RequestParam(value = "query", required = false) String searchQuery,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            Model model) {

        Page<HotelDTO> hotelPage;

        if (searchQuery != null && !searchQuery.isEmpty()) {
            hotelPage = hotelService.searchHotelsByAddress(searchQuery, page, size);
        } else {
            hotelPage = hotelService.getAllHotels(page, size);
        }

        model.addAttribute("hotels", hotelPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", hotelPage.getTotalPages());
        model.addAttribute("query", searchQuery);
        model.addAttribute("resultsCount", hotelPage.getTotalElements());

        return "web/search_results";
    }
}
