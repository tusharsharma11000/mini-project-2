package com.example.CRUD.controller;

import com.example.CRUD.dto.PostResponseDto;
import com.example.CRUD.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'UNVERIFIED')")
    public ResponseEntity<PostResponseDto> create(
            @RequestParam(value = "content", required = false, defaultValue = "") String content,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "mediaUrl", required = false) String mediaUrl,
            Authentication authentication) {

        String username = authentication.getName();
        if (file != null && !file.isEmpty()) {
            return ResponseEntity.ok(postService.create(content, file, username));
        } else if (mediaUrl != null && !mediaUrl.isBlank()) {
            return ResponseEntity.ok(postService.createWithUrl(content, mediaUrl, username));
        } else {
            return ResponseEntity.ok(postService.create(content, null, username));
        }
    }

    @PostMapping(value = "/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'UNVERIFIED')")
    public ResponseEntity<PostResponseDto> createJson(
            @RequestBody Map<String, String> body,
            Authentication authentication) {

        String username = authentication.getName();
        String content = body.getOrDefault("content", "");
        String mediaUrl = body.getOrDefault("mediaUrl", null);

        return ResponseEntity.ok(postService.createWithUrl(content, mediaUrl, username));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<PostResponseDto> update(
            @PathVariable Long id,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Authentication authentication) {

        String username = authentication.getName();
        return ResponseEntity.ok(postService.update(id, content, file, username));
    }

    @GetMapping
    public ResponseEntity<List<PostResponseDto>> findAll(Authentication authentication) {
        String currentUsername = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(postService.findAll(currentUsername));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDto> findById(
            @PathVariable Long id,
            Authentication authentication) {
        String currentUsername = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(postService.findById(id, currentUsername));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<PostResponseDto> delete(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(postService.delete(id, username));
    }
}
