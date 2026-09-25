package com.example.awsdemo.controller;

import io.awspring.cloud.s3.S3Template;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@RestController
@RequestMapping("/files")
public class FileUploadController {

    private final S3Template s3Template;
    private final String bucketName;

    public FileUploadController(S3Template s3Template,
                                @Value("${app.s3.bucket}") String bucketName) {
        this.s3Template = s3Template;
        this.bucketName = bucketName;
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            this.s3Template.upload(this.bucketName, Objects.requireNonNull(file.getOriginalFilename()), inputStream);
        }
        return "Uploaded: " + file.getOriginalFilename();
    }

    @GetMapping("/download/{key}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String key) {
        Resource resource = this.s3Template.download(this.bucketName, key);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + key + "\"")
                .body(resource);
    }
}