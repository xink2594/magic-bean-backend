package com.example.magicbeanbackend.controller;

import com.example.magicbeanbackend.common.ApiResponse;
import com.example.magicbeanbackend.dto.ImageUploadResponse;
import com.example.magicbeanbackend.service.ImageUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/image")
public class ImageController {

    private final ImageUploadService imageUploadService;

    public ImageController(ImageUploadService imageUploadService) {
        this.imageUploadService = imageUploadService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<ImageUploadResponse>> upload(@RequestParam("file") MultipartFile file)
            throws Exception {
        String url = imageUploadService.uploadImage(file);
        return ResponseEntity.ok(ApiResponse.success(new ImageUploadResponse(url)));
    }
}
