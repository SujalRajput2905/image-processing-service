package com.sujalrajput.imageprocessing.controller;

import com.sujalrajput.imageprocessing.domain.Image;
import com.sujalrajput.imageprocessing.domain.User;
import com.sujalrajput.imageprocessing.dto.ImageReponse;
import com.sujalrajput.imageprocessing.repository.UserRepository;
import com.sujalrajput.imageprocessing.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/images")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam MultipartFile file) {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        String username = authentication.getName();

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(()-> new RuntimeException("User not found"));

        Image image = imageService.uploadImage(file, user);

        ImageReponse response = new ImageReponse(
                image.getId(),
                image.getFileName(),
                image.getOriginalFileName(),
                image.getFileSize(),
                image.getFileType(),
                image.getCreatedAt()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ImageReponse>> getMyImages() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        String username = authentication.getName();

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(()-> new RuntimeException("User not found"));

        List<Image> images = imageService.getUserImages(user);

        List<ImageReponse> response = images.stream()
                .map(image -> new ImageReponse(
                        image.getId(),
                        image.getFileName(),
                        image.getOriginalFileName(),
                        image.getFileSize(),
                        image.getFileType(),
                        image.getCreatedAt()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/file/{fileName}")
    public ResponseEntity<Resource> getImage(@PathVariable String fileName) {
        Resource resource = imageService.getImageFile(fileName);

        MediaType mediaType;
        if(fileName.endsWith(".png")) {
            mediaType = MediaType.IMAGE_PNG;
        } else if (fileName.endsWith(".jpg")) {
            mediaType = MediaType.IMAGE_JPEG;
        } else {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity
                .ok()
                .contentType(mediaType)
                .body(resource);
    }
}
