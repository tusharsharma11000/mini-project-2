package com.example.CRUD.controller;

import com.example.CRUD.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/like")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/toggle")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'UNVERIFIED')")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @RequestParam Long postId,
            Authentication authentication) {

        String username = authentication.getName();
        String status = likeService.toggleLike(username, postId);
        boolean liked = "Liked".equalsIgnoreCase(status);
        long count = likeService.getLikeCount(postId);

        return ResponseEntity.ok(Map.of(
                "status", status,
                "liked", liked,
                "likeCount", count,
                "postId", postId
        ));
    }

    @GetMapping("/count/{postId}")
    public ResponseEntity<Long> getLikeCount(@PathVariable Long postId) {
        return ResponseEntity.ok(likeService.getLikeCount(postId));
    }

    @GetMapping("/is-liked")
    public ResponseEntity<Boolean> isLiked(
            @RequestParam Long postId,
            Authentication authentication) {

        if (authentication == null || "anonymousUser".equals(authentication.getName())) {
            return ResponseEntity.ok(false);
        }

        String username = authentication.getName();
        return ResponseEntity.ok(likeService.isLiked(username, postId));
    }
}
