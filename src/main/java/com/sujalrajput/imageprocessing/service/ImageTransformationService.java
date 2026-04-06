package com.sujalrajput.imageprocessing.service;

import net.coobird.thumbnailator.Thumbnailator;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@Service
public class ImageTransformationService {
    public byte[] transform(
            Resource resource,
            Integer width,
            Integer height,
            Double quality,
            Integer rotate
    ) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        var builder = Thumbnails.of(resource.getInputStream());

        //resize
        if(width!=null && height!=null) {
            builder.size(width, height);
        } else {
            builder.scale(1.0);
        }

        //rotate
        if(rotate!=null) {
            builder.rotate(rotate);
        }

        //compress
        if(quality!=null) {
            builder.outputQuality(quality);
        } else {
            builder.outputQuality(1.0);
        }

        builder.toOutputStream(outputStream);
        return outputStream.toByteArray();
    }
}
