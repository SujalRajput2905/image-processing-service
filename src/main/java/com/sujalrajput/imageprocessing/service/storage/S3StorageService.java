package com.sujalrajput.imageprocessing.service.storage;

import com.sujalrajput.imageprocessing.exception.FileUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.MalformedURLException;

@Profile("s3")
@Service
public class S3StorageService implements StorageService {

    @Autowired
    private S3Client s3Client;

    @Value("${aws.bucket-name}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    @Override
    public String save(MultipartFile file, String fileName) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromBytes(file.getBytes()));

            return String.format("https://%s.s3.%s.amazonaws.com/%s",
                    bucketName, region, fileName);

        } catch (IOException e) {
            throw new FileUploadException("Failed to upload file to S3");
        }
    }

    @Override
    public Resource retrieve(String filePath) {
        try {
            return new UrlResource(filePath);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid S3 file URL");
        }
    }

    @Override
    public void delete(String filePath) {
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }
}
