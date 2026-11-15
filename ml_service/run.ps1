Write-Host "Starting Social Media NLP Microservice on http://localhost:8000..." -ForegroundColor Cyan
python -m uvicorn main:app --host 0.0.0.0 --port 8000 --reload
