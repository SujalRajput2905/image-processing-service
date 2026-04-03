package com.sujalrajput.imageprocessing.service.storage;


import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    String save(MultipartFile file, String filename);

    Resource retrieve(String filePath);

    void delete(String filePath);
}
