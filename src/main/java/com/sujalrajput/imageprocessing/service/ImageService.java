package com.sujalrajput.imageprocessing.service;

import com.sujalrajput.imageprocessing.domain.Image;
import com.sujalrajput.imageprocessing.domain.User;
import com.sujalrajput.imageprocessing.dto.ImageResponse;
import com.sujalrajput.imageprocessing.dto.PagedImageResponse;
import com.sujalrajput.imageprocessing.exception.FileUploadException;
import com.sujalrajput.imageprocessing.exception.ImageNotFoundException;
import com.sujalrajput.imageprocessing.repository.ImageRepository;
import com.sujalrajput.imageprocessing.repository.UserRepository;
import com.sujalrajput.imageprocessing.service.storage.StorageService;
import jakarta.persistence.SecondaryTable;
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
import java.util.Set;
import java.util.UUID;

@Service
public class ImageService {
    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StorageService storageService;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    public Image uploadImage(MultipartFile file, User user) {
        if(file.isEmpty()) {
            throw new FileUploadException("Uploaded file is empty");
        }

        if(file.getSize() > MAX_FILE_SIZE) {
            throw new FileUploadException("File size exceeds 10MB limit");
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

        if(!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new FileUploadException("Invalid File Format");
        }
        String uniqueFileName = UUID.randomUUID() + "." + extension;

        String storedPath = storageService.save(file, uniqueFileName);

        Image image = new Image();
        image.setFileName(uniqueFileName);
        image.setOriginalFileName(originalFileName);
        image.setFileType(extension);
        image.setFilePath(storedPath);
        image.setFileSize(file.getSize());
        image.setUser(user);

        return imageRepository.save(image);
    }

    public PagedImageResponse getUserImages(String username, int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0 || size > 50) size = 10;
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
        Resource resource = storageService.retrieve(filePath);
        return resource;
    }

    public void deleteImage(String fileName, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ImageNotFoundException("User not found"));

        Image image = imageRepository.findByFileNameAndUser(fileName, user)
                .orElseThrow(() -> new ImageNotFoundException("Image not found"));


        String filePath = image.getFilePath();
        storageService.delete(filePath);
        imageRepository.delete(image);
    }
}
