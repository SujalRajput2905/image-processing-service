package com.sujalrajput.imageprocessing.service;

import com.sujalrajput.imageprocessing.domain.Image;
import com.sujalrajput.imageprocessing.domain.User;
import com.sujalrajput.imageprocessing.exception.FileUploadException;
import com.sujalrajput.imageprocessing.exception.ImageNotFoundException;
import com.sujalrajput.imageprocessing.repository.ImageRepository;
import org.apache.catalina.LifecycleState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
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

    public List<Image> getUserImages(User user) {
        return imageRepository.findAllByUser(user);
    }

    public Resource getImageFile(String fileName) {
        Image image = imageRepository.findByFileName(fileName)
                .orElseThrow(() -> new ImageNotFoundException("Image not Found"));

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
}
