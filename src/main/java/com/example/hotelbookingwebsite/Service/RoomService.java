package com.example.hotelbookingwebsite.Service;

import com.example.hotelbookingwebsite.DTO.RoomDTO;
import com.example.hotelbookingwebsite.Model.*;
import com.example.hotelbookingwebsite.Repository.HotelRepository;
import com.example.hotelbookingwebsite.Repository.ImagesRepository;
import com.example.hotelbookingwebsite.Repository.ManagerRepository;
import com.example.hotelbookingwebsite.Repository.RoomRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomService {
    private final ImagesRepository imagesRepository;
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    @Autowired
    public RoomService(ImagesRepository imagesRepository, RoomRepository roomRepository, HotelRepository hotelRepository) {
        this.imagesRepository = imagesRepository;
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
    }

    public RoomDTO getRoomDTOById(Long rid) {
        Room room = roomRepository.findById(rid)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng với ID: " + rid));
        return convertToRoomDTO(room);
    }

    public List<RoomDTO> getAllRoomByHotel(Hotel hotel) {
        return hotel.getRooms().stream()
                .map(this::convertToRoomDTO)
                .collect(Collectors.toList());
    }

    public RoomDTO convertToRoomDTO(Room room) {
        if (room == null) {
            return null;
        }
        RoomDTO dto = new RoomDTO();
        dto.setRid(room.getRid());
        dto.setRoomName(room.getRoomName());
        dto.setDescription(room.getDescription());
        dto.setStatus(room.getStatus());
        dto.setPrice((float) room.getPrice());
        dto.setHotel(room.getHotel());

        // Lấy ảnh đại diện từ room
        List<Images> imageUrls = getRoomImage(room.getRid());
        dto.setImageUrl(imageUrls.stream()
                .map(img -> "/images/" + img.getImageUrl())
                .collect(Collectors.toList()));

        return dto;
    }

    @Transactional
    public Long saveRoom(RoomDTO roomDTO, Long uid) {
        System.out.println("Saving room with data: " + roomDTO);
        // Tìm khách sạn của người quản lý dựa trên UID
        Hotel hotel = hotelRepository.findByManager_Uid(uid);

        // Tạo đối tượng Room mới và gán các thuộc tính từ RoomDTO
        Room room = new Room();
        room.setRoomName(roomDTO.getRoomName());
        room.setDescription(roomDTO.getDescription());
        room.setPrice(roomDTO.getPrice());
        room.setStatus(Constants.ROOM_STATUS.AVAILABLE);  // Trạng thái mặc định là "available"
        room.setHotel(hotel);  // Gán khách sạn cho phòng

        // Lưu phòng vào cơ sở dữ liệu
        Room savedRoom = roomRepository.save(room);

        // Trả về ID của phòng đã lưu
        return savedRoom.getRid();
    }


    private List<Images> getRoomImage(long rid) {
        return imagesRepository.findImagesByRid(rid);

    }

    public Room getRoomById(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));
    }
}
