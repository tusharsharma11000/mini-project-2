package com.example.CRUD.service;

import com.example.CRUD.dto.CommentResponseDto;
import com.example.CRUD.dto.NlpAnalysisResult;
import com.example.CRUD.entity.Comment;
import com.example.CRUD.entity.Post;
import com.example.CRUD.entity.User;
import com.example.CRUD.repository.CommentRepository;
import com.example.CRUD.repository.PostRepository;
import com.example.CRUD.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final NlpModerationService nlpModerationService;

    public CommentResponseDto toDto(Comment comment, String currentUsername) {
        String authorUsername = comment.getUser() != null ? comment.getUser().getUsername() : "anonymous";
        String authorName = authorUsername;
        String authorProfilePic = null;

        String authorRole = "ROLE_USER";
        boolean isAuthorAdmin = false;

        if (comment.getUser() != null && comment.getUser().getUserInfo() != null) {
            authorName = comment.getUser().getUserInfo().getName();
            authorProfilePic = comment.getUser().getUserInfo().getProfilePic();
            if (comment.getUser().getUserInfo().getRole() != null) {
                authorRole = comment.getUser().getUserInfo().getRole().name();
                isAuthorAdmin = comment.getUser().getUserInfo().getRole() == com.example.CRUD.entity.Role.ROLE_ADMIN;
            }
        }

        User currentUser = currentUsername != null ? userRepository.findByUsername(currentUsername) : null;
        boolean isAdmin = currentUser != null && currentUser.getUserInfo() != null &&
                currentUser.getUserInfo().getRole() == com.example.CRUD.entity.Role.ROLE_ADMIN;

        boolean isOwner = (currentUsername != null && currentUsername.equals(authorUsername)) || isAdmin;

        return CommentResponseDto.builder()
                .id(comment.getId())
                .postId(comment.getPost() != null ? comment.getPost().getId() : null)
                .username(authorUsername)
                .authorName(authorName)
                .authorProfilePic(authorProfilePic)
                .authorRole(authorRole)
                .isAuthorAdmin(isAuthorAdmin)
                .comment(comment.getComment())
                .createdAt(comment.getCreatedAt())
                .sentiment(comment.getSentiment() != null ? comment.getSentiment() : "NEUTRAL")
                .toxicityScore(comment.getToxicityScore() != null ? comment.getToxicityScore() : 0.0)
                .isFlagged(Boolean.TRUE.equals(comment.getIsFlagged()))
                .isOwner(isOwner)
                .build();
    }

    public CommentResponseDto addComment(String username, Long postId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment cannot be empty");
        }

        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        // --- Data Science / AI Content Moderation Gate ---
        NlpAnalysisResult analysis = nlpModerationService.analyze(content.trim());
        boolean isToxicOrNsfw = analysis.isToxic() || analysis.isNsfw();

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setPost(post);
        comment.setComment(content.trim());
        comment.setSentiment(analysis.getSentiment());
        comment.setToxicityScore(analysis.getToxicityScore());
        // If toxic or NSFW, automatically flag and quarantine comment (hidden from public, visible to admin)
        comment.setIsFlagged(isToxicOrNsfw);

        Comment saved = commentRepository.save(comment);
        return toDto(saved, username);
    }

    public CommentResponseDto updateComment(Long commentId, String username, String content) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        if (!comment.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to update this comment");
        }

        // Re-analyze updated comment
        NlpAnalysisResult analysis = nlpModerationService.analyze(content.trim());
        boolean isToxicOrNsfw = analysis.isToxic() || analysis.isNsfw();

        comment.setComment(content.trim());
        comment.setSentiment(analysis.getSentiment());
        comment.setToxicityScore(analysis.getToxicityScore());
        comment.setIsFlagged(isToxicOrNsfw);

        Comment saved = commentRepository.save(comment);
        return toDto(saved, username);
    }

    public void deleteComment(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        User currentUser = userRepository.findByUsername(username);
        boolean isAdmin = currentUser != null && currentUser.getUserInfo() != null &&
                currentUser.getUserInfo().getRole() == com.example.CRUD.entity.Role.ROLE_ADMIN;

        boolean isCommentOwner = comment.getUser() != null && comment.getUser().getUsername().equals(username);
        boolean isPostOwner = comment.getPost() != null &&
                comment.getPost().getUser() != null &&
                comment.getPost().getUser().getUsername().equals(username);

        if (!isCommentOwner && !isPostOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this comment");
        }

        commentRepository.delete(comment);
    }

    public CommentResponseDto approveComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
        comment.setIsFlagged(false);
        Comment saved = commentRepository.save(comment);
        return toDto(saved, "tushar_sharma");
    }

    public List<CommentResponseDto> getCommentsByPost(Long postId, String currentUsername) {
        User currentUser = currentUsername != null ? userRepository.findByUsername(currentUsername) : null;
        boolean isAdmin = currentUser != null && currentUser.getUserInfo() != null &&
                currentUser.getUserInfo().getRole() == com.example.CRUD.entity.Role.ROLE_ADMIN;

        List<Comment> comments;
        if (isAdmin) {
            // Admin sees all comments (including flagged comments for review)
            comments = commentRepository.findByPostId(postId);
        } else {
            // Regular public users never see toxic/flagged comments (automatically removed)
            comments = commentRepository.findByPostIdAndIsFlaggedFalse(postId);
        }

        return comments.stream()
                .map(c -> toDto(c, currentUsername))
                .toList();
    }

    public List<CommentResponseDto> getFlaggedComments() {
        return commentRepository.findByIsFlaggedTrueOrderByCreatedAtDesc()
                .stream()
                .map(c -> toDto(c, "tushar_sharma"))
                .toList();
    }

    public long countComments(Long postId) {
        return commentRepository.findByPostIdAndIsFlaggedFalse(postId).size();
    }
}
