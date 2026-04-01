package com.sujalrajput.imageprocessing.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PagedImageResponse {
    private List<ImageResponse> images;
    private int currentPage;
    private int totalPages;
    private long totalImages;
    private boolean hasNext;
    private boolean hasPrevious;
}
