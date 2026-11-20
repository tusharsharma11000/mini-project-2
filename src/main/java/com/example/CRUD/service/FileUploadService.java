package com.example.CRUD.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {

    @Value("${supabase.url:}")
    private String supabaseUrl;

    @Value("${supabase.key:}")
    private String supabaseKey;

    @Value("${supabase.bucket:PublicMedia}")
    private String bucketName;

    private final WebClient webClient;

    private static final String UPLOAD_DIR = "./uploads";

    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        try {
            // Clean filename and handle extension safely (iOS HEIC, JPG, PNG, etc.)
            String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename().replaceAll("[^a-zA-Z0-9.-]", "_") : "image.jpg";
            if (!originalName.contains(".")) {
                originalName += ".jpg";
            }
            String fileName = UUID.randomUUID() + "_" + originalName;

            // 1. Ensure local uploads directory exists
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 2. Save file to local storage
            Path destination = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            log.info("Uploaded file locally to: {}", destination.toAbsolutePath());

            // 3. Try mirroring to Supabase if configured, otherwise use local URL
            if (supabaseUrl != null && !supabaseUrl.isBlank() && supabaseKey != null && !supabaseKey.isBlank()) {
                try {
                    String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + fileName;
                    webClient.post()
                            .uri(uploadUrl)
                            .header("Authorization", "Bearer " + supabaseKey)
                            .header("apikey", supabaseKey)
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .bodyValue(file.getBytes())
                            .retrieve()
                            .toBodilessEntity()
                            .timeout(Duration.ofSeconds(5))
                            .block();

                    return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + fileName;
                } catch (Exception e) {
                    log.warn("Supabase upload failed, serving from local uploads: {}", e.getMessage());
                }
            }

            // Return relative local path accessible via http://localhost:8081/uploads/{fileName}
            return "/uploads/" + fileName;

        } catch (IOException e) {
            log.error("Error storing uploaded file", e);
            throw new RuntimeException("Failed to store file", e);
        }
    }
}
