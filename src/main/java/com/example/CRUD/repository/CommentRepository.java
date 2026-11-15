package com.example.CRUD.repository;

import com.example.CRUD.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByUserId(Long userId);

    List<Comment> findByPostIdOrderByCreatedAtDesc(Long postId);

    long countByPostId(Long postId);

    long countByPostIdAndIsFlaggedFalse(Long postId);

    List<Comment> findByPostId(Long postId);

    List<Comment> findByPostIdAndIsFlaggedFalse(Long postId);

    List<Comment> findByIsFlaggedTrueOrderByCreatedAtDesc();

    long countByIsFlaggedTrue();
}
