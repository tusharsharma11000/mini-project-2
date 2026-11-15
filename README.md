<div align="center">

# ⚡ PulseAI

### AI-Powered Social Media Platform with Real-Time NLP Content Moderation

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Python](https://img.shields.io/badge/Python-3.11-3776AB?style=for-the-badge&logo=python&logoColor=white)](https://python.org)
[![FastAPI](https://img.shields.io/badge/FastAPI-0.100+-009688?style=for-the-badge&logo=fastapi&logoColor=white)](https://fastapi.tiangolo.com/)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)

**[🚀 Live Demo](#-quick-start) · [📖 API Docs](#-api-reference) · [🤖 AI Features](#-ai--nlp-features) · [🌍 Deploy](#-deploy-publicly)**

<br/>

> A production-grade full-stack social platform where **every comment is screened by AI** in real time. Toxic content is automatically intercepted and quarantined before reaching the public feed — only the Admin sees and moderates it.

<br/>

![PulseAI Feed Screenshot](https://images.unsplash.com/photo-1555066931-4365d14bab8c?auto=format&fit=crop&w=1200&q=80)

</div>

---

## 📋 Table of Contents

- [✨ Features](#-features)
- [🏗️ Architecture](#️-architecture)
- [🤖 AI & NLP Features](#-ai--nlp-features)
- [🚀 Quick Start](#-quick-start)
- [📱 Mobile-First Design](#-mobile-first-design)
- [🔑 Authentication](#-authentication)
- [🌍 Deploy Publicly](#-deploy-publicly)
- [📁 Project Structure](#-project-structure)
- [📡 API Reference](#-api-reference)
- [🛠️ Tech Stack](#️-tech-stack)
- [👨‍💻 Author](#-author)

---

## ✨ Features

| Feature | Description |
|---|---|
| 🤖 **AI Content Moderation** | Every comment analyzed for toxicity & NSFW content in real time |
| 🛡️ **Auto-Quarantine** | Toxic comments hidden from public, queued for Admin review |
| 📊 **Sentiment Analysis** | POSITIVE / NEUTRAL / NEGATIVE tagging on all public comments |
| 🔐 **Google Auth** | Sign in with any Google/Gmail account instantly |
| 📱 **Mobile-First UI** | Fully responsive with sticky header + bottom nav bar |
| 🖼️ **Photo Posts** | Upload images from device or paste external URLs (50MB limit) |
| 👑 **Admin Console** | Full moderation dashboard: users, posts, flagged comments |
| 🔁 **Like System** | Optimistic UI like toggling with persistent counting |
| 💬 **Real-Time Comments** | AI-analyzed comments with sentiment badges |
| 🧪 **NLP Test Lab** | Interactive model inference tester in-browser |
| 🏷️ **Topic Filters** | Filter feed by #DataScience, #MachineLearning, #Photography, etc. |
| 📈 **Platform Analytics** | Live safety index, toxicity rate, sentiment distribution charts |

---

## 🏗️ Architecture

```
                        ┌─────────────────────────────────┐
                        │       Client Browser / Mobile   │
                        │  Single Page App (Tailwind CSS) │
                        └──────────────┬──────────────────┘
                                       │  REST API + JWT
                                       ▼
                        ┌─────────────────────────────────┐
                        │  Spring Boot Backend  :8081      │
                        │  ┌─────────────────────────┐    │
                        │  │  Security Layer (JWT)   │    │
                        │  │  Auth / Register        │    │
                        │  │  Google OAuth Endpoint  │    │
                        │  └─────────────────────────┘    │
                        │  ┌─────────────────────────┐    │
                        │  │  REST Controllers       │    │
                        │  │  Posts / Comments       │    │
                        │  │  Likes / Users          │    │
                        │  │  Admin / NLP            │    │
                        │  └─────────────────────────┘    │
                        └──────┬──────────────┬────────────┘
                               │              │
              ┌────────────────┘              └──────────────────┐
              ▼                                                   ▼
  ┌───────────────────────┐                       ┌──────────────────────────┐
  │  Python FastAPI  :8000│                       │   H2 File Database       │
  │  NLP Microservice     │                       │   ./data/socialdb.mv.db  │
  │  ┌─────────────────┐  │                       │   Users & Profiles       │
  │  │ RoBERTa Model   │  │                       │   Posts, Likes           │
  │  │ DistilBERT      │  │                       │   Comments + Flags       │
  │  │ Fallback NLP    │  │                       │   Tokens                 │
  │  └─────────────────┘  │                       └──────────────────────────┘
  └───────────────────────┘
```

---

## 🤖 AI & NLP Features

### How the Toxicity Guardrail Works

```
User submits comment
        │
        ▼
  POST /comments  ──►  NlpModerationService.analyze(text)
                                │
                      ┌─────────▼──────────┐
                      │   FastAPI ML API   │  ← If online (port 8000)
                      │   RoBERTa / BERT   │
                      └─────────┬──────────┘
                                │  (fallback if offline)
                      ┌─────────▼──────────┐
                      │  Heuristic Engine  │
                      │  Keyword matching  │
                      │  Sentiment scoring │
                      └─────────┬──────────┘
                                │
              ┌─────────────────▼──────────────────┐
              │  Toxicity Score ≥ 70% OR NSFW?     │
              └───────────┬──────────────┬──────────┘
                        YES              NO
                          │              │
              ┌───────────▼──┐    ┌──────▼──────────────┐
              │  isFlagged=  │    │  Save comment        │
              │  true        │    │  with sentiment tag  │
              │  Hidden from │    │  Visible to public   │
              │  public feed │    └─────────────────────┘
              │  Admin-only  │
              └──────────────┘
```

### Admin Moderation Queue
When a toxic comment is flagged:
- 🚫 **Public users** see nothing — the comment simply doesn't appear
- 👑 **Admin** sees it in the feed with a red `🛡️ AUTO-REMOVED BY AI` banner
- 📋 **Admin Console** shows all flagged comments with toxicity score, user info, and actions:
  - **Restore** → Makes the comment public (overrides AI decision)
  - **Purge** → Permanently deletes it from the database

### NLP Lab (Interactive Testing)
Visit the **AI Moderation Lab** tab to:
- Test any text against the live model in real time
- See toxicity score, sentiment polarity, and model explanation
- Use preset benchmark tests (Positive / Neutral / Toxic)

---

## 🚀 Quick Start

### Prerequisites

| Tool | Version | Required? |
|---|---|---|
| Java JDK | 17+ | ✅ Required |
| Maven | (bundled via `mvnw`) | ✅ Required |
| Python | 3.9+ | ⚡ Optional (for ML) |
| pip | latest | ⚡ Optional |

### Option A — 1-Click Launch (Windows)

```bat
start-all.bat
```

This opens two terminal windows:
- Spring Boot backend on **`:8081`**
- Python NLP service on **`:8000`** (optional, enhanced AI)

### Option B — Manual Launch

**Step 1: Start Spring Boot backend**
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / Mac
./mvnw spring-boot:run
```

Open **[http://localhost:8081](http://localhost:8081)** ✅

**Step 2: (Optional) Start Python NLP Microservice**
```bash
cd ml_service
pip install -r requirements.txt
python -m uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

API docs available at **[http://localhost:8000/docs](http://localhost:8000/docs)** ✅

> **Note:** If the Python service is offline, the system automatically falls back to a built-in heuristic NLP engine — the platform still works fully.

---

## 📱 Mobile-First Design

PulseAI is built to be **used on phones** — not just desktop.

### Mobile Layout
- **Sticky top header** with PulseAI logo, live NLP status, and quick post button
- **Fixed bottom navigation bar** with 5 tabs:
  - 🏠 Feed → Home timeline
  - 🤖 AI Lab → Live NLP tester
  - ➕ Post pill → Floating center action button
  - 📊 Stats → Platform analytics
  - 👑 Admin → Admin console (admin only)
- All modals and dialogs are touch-optimized with large tap targets
- 50MB photo uploads from mobile camera roll

### Responsive Breakpoints
| Screen | Layout |
|---|---|
| `< 768px` | Mobile: sticky header + bottom nav |
| `768px - 1280px` | Tablet: left sidebar visible |
| `> 1280px` | Desktop: full 3-column layout |

---

## 🔑 Authentication

PulseAI supports **three authentication methods**:

### 1. Google Sign-In (Recommended for public users)
- Click the **Google Sign In** tab in the auth modal
- Enter your Gmail address (or use the Google button if configured)
- Account is automatically created with `ROLE_USER`
- No password needed — just your Google email

### 2. Register (Custom account)
- Choose a username, email, and password
- Instant access after registration
- Gets `ROLE_USER` role

### 3. Master Admin (Protected)
- Click the **👑 Master Admin** tab
- Use the 1-click button for `tushar_sharma`
- Or enter credentials manually

### Role Permissions

| Feature | Public User | Admin |
|---|---|---|
| View feed | ✅ | ✅ |
| Create posts | ✅ | ✅ |
| Like posts | ✅ | ✅ |
| Comment (AI-screened) | ✅ | ✅ |
| Delete own content | ✅ | ✅ |
| Delete any content | ❌ | ✅ |
| See flagged comments | ❌ | ✅ |
| Moderate comments | ❌ | ✅ |
| Ban users | ❌ | ✅ |
| Admin console | ❌ | ✅ |

---

## 🌍 Deploy Publicly

> **Goal:** Let the whole world use your platform. Here are the best free options.

### Option 1 — Railway (Easiest, Recommended ⭐)

Railway gives you a free tier with a public URL in under 5 minutes.

1. Go to **[railway.app](https://railway.app)** and sign up with GitHub
2. Click **"New Project" → "Deploy from GitHub repo"**
3. Select your `mini-project-2` repo
4. Railway auto-detects Spring Boot and builds it
5. Click **"Generate Domain"** to get a public URL like `https://pulseai-production.up.railway.app`

**Environment Variables to set in Railway:**
```
SERVER_PORT=8080
SPRING_DATASOURCE_URL=jdbc:h2:file:./data/socialdb;MODE=PostgreSQL
SPRING_DATASOURCE_USERNAME=sa
SPRING_DATASOURCE_PASSWORD=
SPRING_JPA_HIBERNATE_DDL_AUTO=update
```

6. *(Optional)* Deploy the Python NLP service as a second Railway service from the `ml_service/` folder

---

### Option 2 — Render (Free tier available)

1. Go to **[render.com](https://render.com)** → New → **Web Service**
2. Connect your GitHub repo
3. Set:
   - **Build Command:** `./mvnw clean package -DskipTests`
   - **Start Command:** `java -jar target/CRUD-0.0.1-SNAPSHOT.jar`
4. Add environment variables (same as above)
5. Deploy → Get your public URL

---

### Option 3 — Fly.io (Good free tier)

```bash
# Install flyctl
# https://fly.io/docs/hands-on/install-flyctl/

fly auth login
fly launch
fly deploy
```

---

### Option 4 — Make it accessible on your local network (Quick test)

If you just want friends on the same Wi-Fi to access it:

```bash
# Find your local IP address
ipconfig    # Windows
ifconfig    # Mac/Linux

# Start Spring Boot
.\mvnw.cmd spring-boot:run

# Share this URL with friends on same network:
# http://YOUR_IP_ADDRESS:8081
```

---

### Option 5 — ngrok (Instant public tunnel, great for demos)

```bash
# Install ngrok: https://ngrok.com/download
ngrok http 8081

# You'll get a URL like:
# https://abc123.ngrok.io
# Share this with anyone in the world!
```

---

### After Deploying: Enable Real Google Sign-In

Currently Google Sign-In works with **direct email input** (no OAuth popup needed). To enable the actual Google popup button:

1. Go to [console.cloud.google.com](https://console.cloud.google.com)
2. Create a project → **APIs & Services** → **Credentials**
3. Create **OAuth 2.0 Client ID** (Web Application)
4. Add your deployed URL to **Authorized Origins** and **Redirect URIs**
5. Copy your **Client ID**
6. In `index.html`, find `YOUR_GOOGLE_CLIENT_ID` and replace it

---

## 📁 Project Structure

```
mini-project-2-main/
│
├── src/main/
│   ├── java/com/example/CRUD/
│   │   ├── auth/
│   │   │   ├── JwtFilter.java           # JWT token validation filter
│   │   │   └── JwtUtil.java             # Token generation & parsing
│   │   │
│   │   ├── config/
│   │   │   ├── DataInitializer.java     # Seed demo users & posts on startup
│   │   │   ├── SecurityConfig.java      # Spring Security + CORS config
│   │   │   ├── WebClientConfig.java     # HTTP client for NLP service
│   │   │   └── WebMvcConfig.java        # Static file / upload serving
│   │   │
│   │   ├── controller/
│   │   │   ├── AdminController.java     # Admin-only endpoints
│   │   │   ├── AuthController.java      # Login, Register, Google OAuth
│   │   │   ├── CommentController.java   # Comment CRUD
│   │   │   ├── LikeController.java      # Like/unlike toggle
│   │   │   ├── NlpController.java       # NLP test & stats endpoints
│   │   │   ├── PostController.java      # Post CRUD + file upload
│   │   │   └── UserController.java      # User profile endpoints
│   │   │
│   │   ├── dto/                         # Data Transfer Objects
│   │   ├── entity/                      # JPA Entities (User, Post, Comment...)
│   │   ├── repository/                  # Spring Data JPA Repositories
│   │   └── service/
│   │       ├── CommentService.java      # AI moderation + comment logic
│   │       ├── FileUploadService.java   # Local file + Supabase upload
│   │       ├── NlpModerationService.java # Calls Python NLP + fallback
│   │       ├── PostService.java         # Post creation & feed logic
│   │       └── UserService.java         # User registration & profiles
│   │
│   └── resources/
│       ├── application.properties       # DB, mail, upload config
│       └── static/
│           └── index.html               # Entire frontend SPA (Tailwind CSS)
│
├── ml_service/
│   ├── main.py                          # FastAPI NLP microservice
│   └── requirements.txt                 # Python dependencies
│
├── data/                                # H2 database file (auto-created)
├── uploads/                             # Uploaded images (auto-created)
├── start-all.bat                        # 1-click launcher (Windows)
├── run-app.bat                          # Backend only launcher
├── run-ml-service.bat                   # Python NLP only launcher
└── pom.xml                              # Maven build config
```

---

## 📡 API Reference

### Authentication
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/auth/login` | Username/password login | Public |
| `POST` | `/auth/register` | Create new account | Public |
| `POST` | `/auth/google` | Google email/OAuth sign-in | Public |
| `GET` | `/auth/me` | Get current user info | 🔐 JWT |
| `PUT` | `/auth/profile` | Update name/avatar/location | 🔐 JWT |
| `POST` | `/auth/avatar` | Upload profile picture | 🔐 JWT |

### Posts
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `GET` | `/post` | Get all posts (feed) | Public |
| `POST` | `/post` | Create post with image upload | 🔐 JWT |
| `POST` | `/post/json` | Create post with URL media | 🔐 JWT |
| `PUT` | `/post/{id}` | Update post | 🔐 JWT |
| `DELETE` | `/post/{id}` | Delete post | 🔐 JWT |

### Comments (AI-Screened)
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `GET` | `/comments/post/{postId}` | Get comments for a post | Public |
| `POST` | `/comments` | Submit comment (AI-moderated) | 🔐 JWT |
| `DELETE` | `/comments/{id}` | Delete comment | 🔐 JWT |

### Likes
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/like/toggle/{postId}` | Toggle like on post | 🔐 JWT |
| `GET` | `/like/count/{postId}` | Get like count | Public |

### Admin (Admin Role Required)
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/admin/overview` | Platform stats dashboard |
| `GET` | `/api/admin/users` | All registered users |
| `DELETE` | `/api/admin/users/{id}` | Ban/delete user |
| `PUT` | `/api/admin/users/{id}/role` | Change user role |
| `GET` | `/api/admin/flagged-comments` | AI-flagged toxic comments |
| `PUT` | `/api/admin/flagged-comments/{id}/approve` | Restore flagged comment |
| `DELETE` | `/api/admin/flagged-comments/{id}` | Permanently delete |

### NLP / AI
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/api/nlp/analyze` | Analyze any text for toxicity | Public |
| `GET` | `/api/nlp/stats` | Platform NLP statistics | Public |

---

## 🛠️ Tech Stack

### Backend
| Technology | Purpose |
|---|---|
| **Spring Boot 4.0.2** | Main REST API framework |
| **Spring Security** | JWT authentication & RBAC |
| **Spring Data JPA** | Database ORM |
| **H2 Database** | Embedded file-based SQL database |
| **PostgreSQL** | Production database (optional) |
| **Spring WebFlux** | Reactive HTTP client for NLP calls |
| **Spring Mail** | Email verification |
| **JJWT 0.11.5** | JWT token library |
| **Lombok** | Boilerplate reduction |

### Frontend
| Technology | Purpose |
|---|---|
| **Tailwind CSS (CDN)** | Utility-first styling |
| **Vanilla JavaScript** | No-framework SPA logic |
| **Google Identity Services** | OAuth sign-in integration |
| **Fetch API** | REST API calls |

### ML / AI Microservice
| Technology | Purpose |
|---|---|
| **Python 3.9+** | ML service runtime |
| **FastAPI** | High-performance API framework |
| **Uvicorn** | ASGI server |
| **HuggingFace Transformers** | RoBERTa / DistilBERT models |
| **PyTorch** | Deep learning backend |

---

## 🏆 What Makes This Special

This isn't just another CRUD app. PulseAI demonstrates a **production-grade microservices architecture** with real AI integration:

1. **Decoupled AI Pipeline** — The NLP service runs independently and can be swapped, scaled, or upgraded without touching the main backend
2. **Resilient Fallback** — The platform never goes down even if the ML service is offline
3. **Zero-Trust Moderation** — AI makes moderation decisions automatically; humans (admin) only override when needed
4. **Mobile-First** — Designed to be deployed publicly and used on phones
5. **Role-Based Security** — Multi-layer access control from JWT filter through method-level security

---

## 📸 Screenshots

| Feed (Mobile) | AI Moderation Lab | Admin Console |
|---|---|---|
| Responsive bottom nav | Live NLP inference tester | Flagged comment queue |

---

## 📄 License

This project is open source under the **MIT License** — feel free to fork, modify, and deploy it as your own platform.

---

## 👨‍💻 Author

<div align="center">

**Tushar Sharma**

*Full-Stack Developer & AI/ML Enthusiast*

[![GitHub](https://img.shields.io/badge/GitHub-tusharsharma11000-181717?style=for-the-badge&logo=github)](https://github.com/tusharsharma11000)

---

*Registration No: 12306719 | Roll No: 21*

*Built with ❤️ using Spring Boot, Python FastAPI, and AI-powered NLP*

</div>

---

<div align="center">

**⭐ Star this repo if you found it useful!**

*PulseAI — Where every comment is screened by AI before the world sees it.*

</div>
