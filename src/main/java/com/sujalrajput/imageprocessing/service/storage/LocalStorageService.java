package com.sujalrajput.imageprocessing.service.storage;

import com.sujalrajput.imageprocessing.exception.FileUploadException;
import com.sujalrajput.imageprocessing.exception.ImageNotFoundException;
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

@Service
public class LocalStorageService implements StorageService {

    private static final String UPLOAD_DIR = "uploads";

    @Override
    public String save(MultipartFile file, String filename) {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new FileUploadException("Failed to create Directory");
        }

        Path filePath = uploadPath.resolve(filename);
        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileUploadException("Failed to store file");
        }
        return filePath.toString();
    }

    @Override
    public Resource retrieve(String filePath) {
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

    @Override
    public void delete(String filePath) {
        Path path = Paths.get(filePath);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new FileUploadException("Failed to delete the file from the disk");
        }
    }
}
