package com.example.CRUD.controller;

import com.example.CRUD.entity.Role;
import com.example.CRUD.entity.User;
import com.example.CRUD.repository.CommentRepository;
import com.example.CRUD.repository.LikeRepository;
import com.example.CRUD.repository.PostRepository;
import com.example.CRUD.repository.UserRepository;
import com.example.CRUD.service.NlpModerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final NlpModerationService nlpModerationService;
    private final com.example.CRUD.service.CommentService commentService;

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getAdminOverview() {
        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("totalUsers", userRepository.count());
        overview.put("totalPosts", postRepository.count());
        overview.put("totalComments", commentRepository.count());
        overview.put("totalLikes", likeRepository.count());
        overview.put("flaggedCommentsCount", commentRepository.countByIsFlaggedTrue());
        overview.putAll(nlpModerationService.getPlatformAnalytics());
        return ResponseEntity.ok(overview);
    }

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<Map<String, Object>> userList = userRepository.findAll().stream().map(user -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("email", user.getEmail());
            if (user.getUserInfo() != null) {
                map.put("name", user.getUserInfo().getName());
                map.put("role", user.getUserInfo().getRole() != null ? user.getUserInfo().getRole().name() : "ROLE_USER");
                map.put("profilePic", user.getUserInfo().getProfilePic());
                map.put("location", user.getUserInfo().getLocation());
            } else {
                map.put("name", user.getUsername());
                map.put("role", "ROLE_USER");
            }
            map.put("postCount", user.getPosts() != null ? user.getPosts().size() : 0);
            return map;
        }).toList();

        return ResponseEntity.ok(userList);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if ("tushar_sharma".equalsIgnoreCase(user.getUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete master administrator account.");
        }

        userRepository.delete(user);
        return ResponseEntity.ok(Map.of("message", "User " + user.getUsername() + " successfully banned and deleted."));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<Map<String, Object>> changeUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String newRoleStr = body.getOrDefault("role", "ROLE_USER").toUpperCase();
        if (!newRoleStr.startsWith("ROLE_")) {
            newRoleStr = "ROLE_" + newRoleStr;
        }

        Role newRole;
        try {
            newRole = Role.valueOf(newRoleStr);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role. Use ROLE_USER or ROLE_ADMIN.");
        }

        if (user.getUserInfo() != null) {
            user.getUserInfo().setRole(newRole);
            userRepository.save(user);
        }

        return ResponseEntity.ok(Map.of(
                "message", "Role updated for " + user.getUsername(),
                "username", user.getUsername(),
                "role", newRole.name()
        ));
    }

    @GetMapping("/flagged-comments")
    public ResponseEntity<List<com.example.CRUD.dto.CommentResponseDto>> getFlaggedComments() {
        return ResponseEntity.ok(commentService.getFlaggedComments());
    }

    @PutMapping("/flagged-comments/{id}/approve")
    public ResponseEntity<?> approveComment(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.approveComment(id));
    }

    @DeleteMapping("/flagged-comments/{id}")
    public ResponseEntity<Map<String, String>> deleteFlaggedComment(@PathVariable Long id) {
        commentService.deleteComment(id, "tushar_sharma");
        return ResponseEntity.ok(Map.of("message", "Flagged toxic comment deleted successfully."));
    }
}
