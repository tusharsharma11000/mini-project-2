@echo off
title PulseAI - Full Stack Launcher
echo ========================================================
echo   Launching PulseAI (Backend + ML Microservice)
echo ========================================================
start "PulseAI - Python NLP Service" cmd /k "cd ml_service && python -m uvicorn main:app --host 0.0.0.0 --port 8000 --reload"
timeout /t 2 >nul
start "PulseAI - Spring Boot Backend" cmd /k ".\mvnw.cmd spring-boot:run"
echo.
echo Services launched!
echo Web UI will be available at: http://localhost:8081
echo ML API docs available at:    http://localhost:8000/docs
echo ========================================================
pause
