package com.example.CRUD.config;

import com.example.CRUD.entity.*;
import com.example.CRUD.repository.CommentRepository;
import com.example.CRUD.repository.LikeRepository;
import com.example.CRUD.repository.PostRepository;
import com.example.CRUD.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    @Override
    public void run(String... args) {
        try {
            // Check if demo user already exists
            User user1 = userRepository.findByUsername("tushar_sharma");
            if (user1 != null) {
                // Ensure Tushar Sharma has full Admin privileges and latest password
                if (user1.getUserInfo() != null && user1.getUserInfo().getRole() != Role.ROLE_ADMIN) {
                    user1.getUserInfo().setRole(Role.ROLE_ADMIN);
                }
                user1.setPassword("Mohit@2006");
                userRepository.save(user1);
            } else {
                log.info("Seeding initial demo data for social feed and AI moderation...");

                // 1. Create User 1: Tushar Sharma (Project Author)
                user1 = new User();
                user1.setUsername("tushar_sharma");
                user1.setEmail("tushar@example.com");
                user1.setPassword("Mohit@2006");

                UserInfo info1 = new UserInfo();
                info1.setName("Tushar Sharma");
                info1.setPhone("+91 9876543210");
                info1.setLocation("New Delhi, India");
                info1.setProfilePic("https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80");
                info1.setRole(Role.ROLE_ADMIN);
                info1.setUser(user1);
                user1.setUserInfo(info1);
                userRepository.save(user1);

                // 2. Create User 2: Data Wizard (AI Researcher)
                User user2 = new User();
                user2.setUsername("data_wizard");
                user2.setEmail("aria@example.com");
                user2.setPassword("password123");

                UserInfo info2 = new UserInfo();
                info2.setName("Aria Chen");
                info2.setPhone("+1 555-0199");
                info2.setLocation("San Francisco, CA");
                info2.setProfilePic("https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=400&q=80");
                info2.setRole(Role.ROLE_USER);
                info2.setUser(user2);
                user2.setUserInfo(info2);
                userRepository.save(user2);

                // 3. Create User 3: Marcus (Backend Architect)
                User user3 = new User();
                user3.setUsername("tech_lead");
                user3.setEmail("marcus@example.com");
                user3.setPassword("password123");

                UserInfo info3 = new UserInfo();
                info3.setName("Marcus Vance");
                info3.setPhone("+44 20 7946 0912");
                info3.setLocation("London, UK");
                info3.setProfilePic("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=400&q=80");
                info3.setRole(Role.ROLE_ADMIN);
                info3.setUser(user3);
                user3.setUserInfo(info3);
                userRepository.save(user3);

                // 4. Seed Post 1: AI & Data Science
                Post post1 = new Post();
                post1.setUser(user2);
                post1.setContent("Fine-tuning our multi-modal sentiment model today! Achieving 94.2% F1-score on real-time comment moderation. The convergence curve looks stunning 📊✨ #DataScience #MachineLearning #AI #PyTorch");
                post1.setMediaUrl("https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&w=1200&q=80");
                post1.setCreatedAt(LocalDateTime.now().minusHours(3));
                postRepository.save(post1);

                // 5. Seed Post 2: Tech & Architecture
                Post post2 = new Post();
                post2.setUser(user1);
                post2.setContent("Just launched the full-stack Social Media Architecture with Spring Boot 3 + Supabase + Python FastAPI ML inference pipeline! Super responsive feed with real-time AI safety guardrails 🚀🔥 #FullStack #SpringBoot #DevCommunity");
                post2.setMediaUrl("https://images.unsplash.com/photo-1555066931-4365d14bab8c?auto=format&fit=crop&w=1200&q=80");
                post2.setCreatedAt(LocalDateTime.now().minusHours(6));
                postRepository.save(post2);

                // 6. Seed Post 3: Photography / Lifestyle
                Post post3 = new Post();
                post3.setUser(user3);
                post3.setContent("Golden hour from the peaks. Clear skies and fresh air after a long sprint week. Balance is everything 🏔️🌅 #WeekendVibes #Photography #Mountains");
                post3.setMediaUrl("https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=1200&q=80");
                post3.setCreatedAt(LocalDateTime.now().minusHours(12));
                postRepository.save(post3);

                // 7. Seed Comments with Sentiment Tags
                Comment c1 = new Comment();
                c1.setUser(user1);
                c1.setPost(post1);
                c1.setComment("Incredible results! Are you using RoBERTa or DistilBERT for the embeddings?");
                c1.setSentiment("POSITIVE");
                c1.setToxicityScore(0.02);
                c1.setIsFlagged(false);
                commentRepository.save(c1);

                Comment c2 = new Comment();
                c2.setUser(user3);
                c2.setPost(post1);
                c2.setComment("The inference latency under 20ms is the most impressive part.");
                c2.setSentiment("POSITIVE");
                c2.setToxicityScore(0.01);
                c2.setIsFlagged(false);
                commentRepository.save(c2);

                Comment c3 = new Comment();
                c3.setUser(user2);
                c3.setPost(post2);
                c3.setComment("The clean UI and reactive feed feel so smooth. Great job!");
                c3.setSentiment("POSITIVE");
                c3.setToxicityScore(0.03);
                c3.setIsFlagged(false);
                commentRepository.save(c3);

                // 8. Seed Likes
                Like l1 = new Like();
                l1.setUser(user1);
                l1.setPost(post1);
                likeRepository.save(l1);

                Like l2 = new Like();
                l2.setUser(user3);
                l2.setPost(post1);
                likeRepository.save(l2);

                Like l3 = new Like();
                l3.setUser(user2);
                l3.setPost(post2);
                likeRepository.save(l3);

                log.info("Demo data successfully seeded.");
            }
        } catch (Exception e) {
            log.warn("Demo data seeding skipped or already present: {}", e.getMessage());
        }
    }
}
