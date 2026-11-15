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
public class PostResponseDto {
    private Long id;
    private String content;
    private String mediaUrl;
    private LocalDateTime createdAt;
    private String username;
    private String authorName;
    private String authorProfilePic;
    private String authorRole;
    private boolean isAuthorAdmin;
    private long likeCount;
    private long commentCount;
    private boolean isLiked;
    private boolean isOwner;
}
