package com.sujalrajput.imageprocessing.service;

import com.sujalrajput.imageprocessing.domain.Image;
import com.sujalrajput.imageprocessing.domain.User;
import com.sujalrajput.imageprocessing.exception.FileUploadException;
import com.sujalrajput.imageprocessing.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
}
