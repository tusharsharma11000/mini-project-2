package com.example.CRUD.controller;

import com.example.CRUD.dto.CommentRequestDto;
import com.example.CRUD.dto.CommentResponseDto;
import com.example.CRUD.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'UNVERIFIED')")
    public ResponseEntity<CommentResponseDto> addComment(
            @RequestParam(required = false) Long postId,
            @RequestBody Map<String, Object> body,
            Authentication authentication) {

        String username = authentication != null ? authentication.getName() : "anonymous";

        Long targetPostId = postId;
        if (targetPostId == null && body.containsKey("postId")) {
            targetPostId = Long.valueOf(body.get("postId").toString());
        }

        String content = body.containsKey("content")
                ? body.get("content").toString()
                : (body.containsKey("comment") ? body.get("comment").toString() : "");

        CommentResponseDto response = commentService.addComment(username, targetPostId, content);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{commentId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, String> body,
            Authentication authentication) {

        String username = authentication.getName();
        String content = body.getOrDefault("content", body.getOrDefault("comment", ""));

        CommentResponseDto updated = commentService.updateComment(commentId, username, content);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Map<String, String>> deleteComment(
            @PathVariable Long commentId,
            Authentication authentication) {

        String username = authentication.getName();
        commentService.deleteComment(commentId, username);

        return ResponseEntity.ok(Map.of("message", "Comment deleted successfully", "commentId", commentId.toString()));
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentResponseDto>> getCommentsByPost(
            @PathVariable Long postId,
            Authentication authentication) {

        String currentUsername = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(commentService.getCommentsByPost(postId, currentUsername));
    }

    @GetMapping("/count/{postId}")
    public ResponseEntity<Long> countComments(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.countComments(postId));
    }
}
