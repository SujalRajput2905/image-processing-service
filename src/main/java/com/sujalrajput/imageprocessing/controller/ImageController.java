package com.sujalrajput.imageprocessing.controller;

import com.sujalrajput.imageprocessing.domain.Image;
import com.sujalrajput.imageprocessing.domain.User;
import com.sujalrajput.imageprocessing.dto.ImageResponse;
import com.sujalrajput.imageprocessing.dto.PagedImageResponse;
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

        ImageResponse response = new ImageResponse(
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
    public ResponseEntity<PagedImageResponse> getMyImages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        PagedImageResponse response = imageService.getUserImages(auth.getName(), page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/file/{fileName}")
    public ResponseEntity<Resource> getImage(@PathVariable String fileName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Resource resource = imageService.getImageFile(fileName, auth.getName());

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

    @DeleteMapping("/file/{fileName}")
    public ResponseEntity<?> deleteImage(@PathVariable String fileName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        imageService.deleteImage(fileName, auth.getName());

        return ResponseEntity
                .noContent()
                .build();
    }
}
