package com.example.CRUD.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity
@Getter
@Setter
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    private LocalDateTime createdAt;

    @Column(length = 20)
    private String sentiment; // "POSITIVE", "NEUTRAL", "NEGATIVE"

    private Double toxicityScore; // 0.0 to 1.0

    private Boolean isFlagged = false;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.sentiment == null) {
            this.sentiment = "NEUTRAL";
        }
        if (this.toxicityScore == null) {
            this.toxicityScore = 0.0;
        }
    }


}
