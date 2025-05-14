package com.example.hotelbookingwebsite.Service;

import com.example.hotelbookingwebsite.Model.Images;
import com.example.hotelbookingwebsite.Repository.ImagesRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageService {

    private final String IMAGE_DIR = "D:/hbw_images/";

    @Autowired
    private ImagesRepository imagesRepository;

    public String uploadImage(MultipartFile file) throws IOException {
        File directory = new File(IMAGE_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(IMAGE_DIR, filename);

        Files.copy(file.getInputStream(), filePath);

        return filename;
    }

    @Transactional
    public Images saveImage(Images image) {
        return imagesRepository.save(image);
    }
}
