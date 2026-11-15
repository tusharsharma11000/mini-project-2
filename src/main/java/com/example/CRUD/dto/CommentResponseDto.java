package com.example.CRUD.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponseDto {
    private Long id;
    private Long postId;
    private String username;
    private String authorName;
    private String authorProfilePic;
    private String authorRole;
    private Boolean isAuthorAdmin;
    private String comment;
    private LocalDateTime createdAt;
    private String sentiment;      // "POSITIVE", "NEUTRAL", "NEGATIVE"
    private Double toxicityScore;  // 0.0 to 1.0
    private Boolean isFlagged;
    private Boolean isOwner;
}
