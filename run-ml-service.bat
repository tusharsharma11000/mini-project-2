@echo off
title PulseAI - Python NLP Microservice
echo ========================================================
echo   Starting Python FastAPI NLP Microservice (Port 8000)...
echo ========================================================
cd ml_service
python -m uvicorn main:app --host 0.0.0.0 --port 8000 --reload
pause
