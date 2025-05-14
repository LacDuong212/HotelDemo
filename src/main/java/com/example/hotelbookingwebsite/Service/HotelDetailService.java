package com.example.hotelbookingwebsite.Service;

import com.example.hotelbookingwebsite.DTO.HotelDetailDTO;
import com.example.hotelbookingwebsite.DTO.RoomDTO;
import com.example.hotelbookingwebsite.Model.Hotel;
import com.example.hotelbookingwebsite.Model.Images;
import com.example.hotelbookingwebsite.Repository.HotelRepository;
import com.example.hotelbookingwebsite.Repository.ImagesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HotelDetailService {
    private final ImagesRepository imagesRepository;
    private final HotelRepository hotelRepository;
    private final RoomService roomService;
    @Autowired
    public HotelDetailService(ImagesRepository imagesRepository, HotelRepository hotelRepository, RoomService roomService) {
        this.imagesRepository = imagesRepository;
        this.hotelRepository = hotelRepository;
        this.roomService = roomService;
    }
    private HotelDetailDTO convertToDTO(Hotel hotel) {
        return new HotelDetailDTO(
                hotel.getHid(),
                hotel.getName(),
                hotel.getAddress(),
                hotel.getDescription(),
                getHotelImage(hotel.getHid()),
                hotel.getManager(),
                roomService.getAllRoomByHotel(hotel)
        );
    }
    public List<Images> getHotelImage(Long hid) {
        List<Images> images = imagesRepository.findImagesByHid(hid);
        images.forEach(img -> img.setImageUrl("/images/" + img.getImageUrl()));
        return images;
    }

    public HotelDetailDTO getHotelById(long id) {
        Hotel hotel = hotelRepository.findById(id).orElseThrow();
        return convertToDTO(hotel);
    }

}
