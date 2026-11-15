"""
FastAPI NLP Content Moderation & Sentiment Analysis Microservice
Designed for integration with Spring Boot Social Media Backend.
"""

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
import time
import re
from typing import Optional, Dict, Any

app = FastAPI(
    title="Social Media NLP & Content Moderation Microservice",
    description="Real-time Toxicity Detection and Sentiment Classification API for Social Feeds",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class AnalyzeRequest(BaseModel):
    text: str = Field(..., description="The comment or post text to analyze")

class AnalyzeResponse(BaseModel):
    text: str
    is_toxic: bool
    toxicity_score: float
    is_nsfw: bool
    nsfw_score: float
    sentiment: str  # POSITIVE, NEUTRAL, NEGATIVE
    sentiment_score: float
    latency_ms: float
    explanation: str
    model_name: str

# In-memory stats
stats = {
    "total_requests": 0,
    "toxic_detected": 0,
    "nsfw_detected": 0,
    "positive_detected": 0,
    "neutral_detected": 0,
    "negative_detected": 0,
    "start_time": time.time()
}

# Try loading Hugging Face transformers pipeline if installed
hf_pipeline = None
try:
    from transformers import pipeline
    print("Loading Hugging Face sentiment & moderation pipeline...")
    hf_pipeline = pipeline("sentiment-analysis", model="distilbert-base-uncased-finetuned-sst-2-english")
    print("Hugging Face model loaded successfully!")
except Exception as e:
    print(f"Transformers pipeline not loaded ({e}). Running on high-performance heuristic NLP engine.")

# Toxic words (Harassment & hate speech)
TOXIC_KEYWORDS = {
    "idiot", "stupid", "hate", "loser", "trash", "ugly", "dumb", "shut up",
    "kill yourself", "die", "bastard", "scam", "scammer", "nasty", "horrible",
    "useless", "clown", "garbage", "freak", "disgusting", "terrible person"
}

# NSFW / Adult content words
NSFW_KEYWORDS = {
    "sex", "sexy", "porn", "xxx", "nude", "naked", "nsfw", "boobs", "penis",
    "vagina", "dick", "pussy", "erotic", "horny", "blowjob", "anal", "tits",
    "slut", "whore", "bitch", "masturbat"
}

POSITIVE_WORDS = {
    "love", "amazing", "beautiful", "great", "awesome", "wonderful", "cool", "fire",
    "congrats", "congratulations", "proud", "stunning", "perfect", "good", "nice",
    "inspiring", "brilliant", "fantastic", "goat", "wholesome", "superb", "epic"
}

NEGATIVE_WORDS = {
    "bad", "worst", "boring", "sad", "disappointed", "annoying", "poor", "lame",
    "mediocre", "cringe", "waste", "failed", "unhappy", "problem"
}

@app.get("/")
def root():
    return {
        "status": "online",
        "service": "Social Media NLP Microservice",
        "endpoints": ["/analyze", "/health", "/stats"]
    }

@app.get("/health")
def health():
    return {"status": "HEALTHY", "uptime_sec": round(time.time() - stats["start_time"], 2)}

@app.get("/stats")
def get_stats():
    return {
        **stats,
        "uptime_minutes": round((time.time() - stats["start_time"]) / 60, 2)
    }

@app.post("/analyze", response_model=AnalyzeResponse)
def analyze_text(payload: AnalyzeRequest):
    start = time.perf_counter()
    text = payload.text.strip()
    stats["total_requests"] += 1

    if not text:
        return AnalyzeResponse(
            text=text,
            is_toxic=False,
            toxicity_score=0.0,
            is_nsfw=False,
            nsfw_score=0.0,
            sentiment="NEUTRAL",
            sentiment_score=1.0,
            latency_ms=0.5,
            explanation="Empty text provided",
            model_name="N/A"
        )

    lower = text.lower()

    # 1. Toxicity Evaluation
    toxic_matched = []
    for pattern in TOXIC_KEYWORDS:
        if re.search(r'\b' + re.escape(pattern) + r'\b', lower):
            toxic_matched.append(pattern)

    is_toxic = len(toxic_matched) > 0
    toxicity_score = min(0.98, 0.70 + (len(toxic_matched) * 0.12)) if is_toxic else 0.03

    # 2. NSFW / Adult Evaluation
    nsfw_matched = []
    for pattern in NSFW_KEYWORDS:
        if re.search(r'\b' + re.escape(pattern) + r'\b', lower):
            nsfw_matched.append(pattern)

    is_nsfw = len(nsfw_matched) > 0
    nsfw_score = 0.96 if is_nsfw else 0.02

    # 3. Sentiment Evaluation
    sentiment = "NEUTRAL"
    sentiment_score = 0.75
    model_source = "NLP Hybrid Engine"

    if hf_pipeline is not None and not is_toxic and not is_nsfw:
        try:
            hf_res = hf_pipeline(text[:512])[0]
            label = hf_res["label"].upper() # POSITIVE or NEGATIVE
            score = float(hf_res["score"])
            sentiment = label
            sentiment_score = score
            model_source = "HuggingFace (DistilBERT-SST2)"
        except Exception:
            pass

    if model_source == "NLP Hybrid Engine":
        pos_hits = sum(1 for w in POSITIVE_WORDS if re.search(r'\b' + re.escape(w) + r'\b', lower))
        neg_hits = sum(1 for w in NEGATIVE_WORDS if re.search(r'\b' + re.escape(w) + r'\b', lower))

        if pos_hits > neg_hits and not is_nsfw:
            sentiment = "POSITIVE"
            sentiment_score = min(0.99, 0.75 + (pos_hits * 0.08))
        elif neg_hits > pos_hits or is_toxic or is_nsfw:
            sentiment = "NEGATIVE"
            sentiment_score = min(0.99, 0.75 + (neg_hits * 0.08))
        else:
            sentiment = "NEUTRAL"
            sentiment_score = 0.70

    # Update stats
    if is_toxic:
        stats["toxic_detected"] += 1
    if is_nsfw:
        stats["nsfw_detected"] += 1
    if sentiment == "POSITIVE":
        stats["positive_detected"] += 1
    elif sentiment == "NEGATIVE":
        stats["negative_detected"] += 1
    else:
        stats["neutral_detected"] += 1

    latency = round((time.perf_counter() - start) * 1000, 2)
    
    if is_nsfw:
        explanation = f"Flagged by Guardrail: Adult / NSFW explicit term detected ('{', '.join(nsfw_matched)}')"
    elif is_toxic:
        explanation = f"Flagged by Guardrail: Offensive / toxic token(s) detected ('{', '.join(toxic_matched)}')"
    else:
        explanation = f"Content cleared safety guardrails ({sentiment.lower()} tone)"

    return AnalyzeResponse(
        text=text,
        is_toxic=is_toxic,
        toxicity_score=round(toxicity_score, 4),
        is_nsfw=is_nsfw,
        nsfw_score=round(nsfw_score, 4),
        sentiment=sentiment,
        sentiment_score=round(sentiment_score, 4),
        latency_ms=latency,
        explanation=explanation,
        model_name=model_source
    )

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
