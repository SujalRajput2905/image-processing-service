package com.sujalrajput.imageprocessing.service;

import com.sujalrajput.imageprocessing.domain.Image;
import com.sujalrajput.imageprocessing.domain.User;
import com.sujalrajput.imageprocessing.dto.ImageResponse;
import com.sujalrajput.imageprocessing.dto.PagedImageResponse;
import com.sujalrajput.imageprocessing.exception.FileUploadException;
import com.sujalrajput.imageprocessing.exception.ImageNotFoundException;
import com.sujalrajput.imageprocessing.repository.ImageRepository;
import com.sujalrajput.imageprocessing.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class ImageService {
    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private UserRepository userRepository;

    public Image uploadImage(MultipartFile file, User user) {
        if(file.isEmpty()) {
            throw new FileUploadException("Uploaded file is empty");
        }

        String originalFileName = file.getOriginalFilename();
        if(originalFileName == null || originalFileName.isBlank()) {
            throw new FileUploadException("Invalid file name");
        }

        int dotIndex = originalFileName.lastIndexOf(".");
        if(dotIndex == -1) {
            throw new FileUploadException("File has no extension");
        }
        String extension = originalFileName.substring(dotIndex + 1);
        String uniqueFileName = UUID.randomUUID() + "." + extension;

        Path uploadPath = Paths.get("uploads");
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new FileUploadException("Failed to create Directory");
        }

        Path filePath = uploadPath.resolve(uniqueFileName);
        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileUploadException("Failed to store file");
        }

        Image image = new Image();
        image.setFileName(uniqueFileName);
        image.setOriginalFileName(originalFileName);
        image.setFileType(extension);
        image.setFilePath(filePath.toString());
        image.setFileSize(file.getSize());
        image.setUser(user);

        return imageRepository.save(image);
    }

    public PagedImageResponse getUserImages(String username, int page, int size) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ImageNotFoundException("User not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Image> imagePage = imageRepository.findAllByUser(user, pageable);

        List<ImageResponse> images = imagePage.getContent()
                .stream()
                .map(image -> new ImageResponse(
                        image.getId(),
                        image.getFileName(),
                        image.getOriginalFileName(),
                        image.getFileSize(),
                        image.getFileType(),
                        image.getCreatedAt()
                ))
                .toList();

        return new PagedImageResponse(
                images,
                imagePage.getNumber(),
                imagePage.getTotalPages(),
                imagePage.getTotalElements(),
                imagePage.hasNext(),
                imagePage.hasPrevious()
        );
    }

    public Resource getImageFile(String fileName, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ImageNotFoundException("User not found"));

        Image image = imageRepository.findByFileNameAndUser(fileName, user)
                .orElseThrow(() -> new ImageNotFoundException("Image not found"));

        String filePath = image.getFilePath();
        Path path = Paths.get(filePath);

        Resource resource;
        try {
            resource = new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            throw new RuntimeException("File Path is invalid");
        }

        if(!resource.exists()) {
            throw new ImageNotFoundException("Image not Found");
        }
        return resource;
    }

    public void deleteImage(String fileName, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ImageNotFoundException("User not found"));

        Image image = imageRepository.findByFileNameAndUser(fileName, user)
                .orElseThrow(() -> new ImageNotFoundException("Image not found"));


        String filePath = image.getFilePath();
        Path path = Paths.get(filePath);

        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new FileUploadException("Failed to delete the file from the disk");
        }
        imageRepository.delete(image);
    }
}
