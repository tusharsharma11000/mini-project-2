package com.example.CRUD.service;

import com.example.CRUD.dto.PostResponseDto;
import com.example.CRUD.entity.Post;
import com.example.CRUD.entity.User;
import com.example.CRUD.repository.CommentRepository;
import com.example.CRUD.repository.LikeRepository;
import com.example.CRUD.repository.PostRepository;
import com.example.CRUD.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final FileUploadService fileUploadService;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    public PostResponseDto toDto(Post post, String currentUsername) {
        String authorUsername = post.getUser() != null ? post.getUser().getUsername() : "anonymous";
        String authorName = authorUsername;
        String authorProfilePic = null;
        String authorRole = "ROLE_USER";
        boolean isAuthorAdmin = false;

        if (post.getUser() != null && post.getUser().getUserInfo() != null) {
            authorName = post.getUser().getUserInfo().getName();
            authorProfilePic = post.getUser().getUserInfo().getProfilePic();
            if (post.getUser().getUserInfo().getRole() != null) {
                authorRole = post.getUser().getUserInfo().getRole().name();
                isAuthorAdmin = post.getUser().getUserInfo().getRole() == com.example.CRUD.entity.Role.ROLE_ADMIN;
            }
        }

        User currentUser = (currentUsername != null && !currentUsername.isBlank())
                ? userRepository.findByUsername(currentUsername)
                : null;
        boolean isViewerAdmin = currentUser != null && currentUser.getUserInfo() != null &&
                currentUser.getUserInfo().getRole() == com.example.CRUD.entity.Role.ROLE_ADMIN;

        long likeCount = likeRepository.countByPostId(post.getId());
        long commentCount = isViewerAdmin
                ? commentRepository.countByPostId(post.getId())
                : commentRepository.countByPostIdAndIsFlaggedFalse(post.getId());

        boolean isLiked = false;
        boolean isOwner = false;

        if (currentUser != null) {
            isLiked = likeRepository.existsByUserAndPost(currentUser, post);
            // Admin has master delete permissions over any post
            isOwner = authorUsername.equals(currentUsername) || isViewerAdmin;
        }

        return PostResponseDto.builder()
                .id(post.getId())
                .content(post.getContent())
                .mediaUrl(post.getMediaUrl())
                .createdAt(post.getCreatedAt())
                .username(authorUsername)
                .authorName(authorName)
                .authorProfilePic(authorProfilePic)
                .authorRole(authorRole)
                .isAuthorAdmin(isAuthorAdmin)
                .likeCount(likeCount)
                .commentCount(commentCount)
                .isLiked(isLiked)
                .isOwner(isOwner)
                .build();
    }

    public List<PostResponseDto> findAll(String currentUsername) {
        return postRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(post -> toDto(post, currentUsername))
                .toList();
    }

    public PostResponseDto findById(Long id, String currentUsername) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        return toDto(post, currentUsername);
    }

    public PostResponseDto create(String content, MultipartFile file, String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        Post post = new Post();
        post.setContent(content != null ? content : "");
        post.setUser(user);

        if (file != null && !file.isEmpty()) {
            try {
                String url = fileUploadService.uploadFile(file);
                post.setMediaUrl(url);
            } catch (Exception e) {
                // If cloud storage upload fails or credentials are placeholder, don't crash post creation
                post.setMediaUrl(null);
            }
        }

        Post saved = postRepository.save(post);
        return toDto(saved, username);
    }

    public PostResponseDto createWithUrl(String content, String mediaUrl, String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        Post post = new Post();
        post.setContent(content != null ? content : "");
        post.setUser(user);
        post.setMediaUrl(mediaUrl);

        Post saved = postRepository.save(post);
        return toDto(saved, username);
    }

    public PostResponseDto update(Long id, String content, MultipartFile file, String username) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        if (!post.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to update this post");
        }

        if (content != null) {
            post.setContent(content);
        }

        if (file != null && !file.isEmpty()) {
            try {
                String url = fileUploadService.uploadFile(file);
                post.setMediaUrl(url);
            } catch (Exception ignored) {}
        }

        Post saved = postRepository.save(post);
        return toDto(saved, username);
    }

    public PostResponseDto delete(Long id, String username) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        User currentUser = userRepository.findByUsername(username);
        boolean isAdmin = currentUser != null && currentUser.getUserInfo() != null &&
                currentUser.getUserInfo().getRole() == com.example.CRUD.entity.Role.ROLE_ADMIN;
        boolean isOwner = post.getUser() != null && post.getUser().getUsername().equals(username);

        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this post");
        }

        PostResponseDto dto = toDto(post, username);
        postRepository.delete(post);
        return dto;
    }
}
