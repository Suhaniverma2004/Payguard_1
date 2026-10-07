FROM python:3.13-slim

ENV PYTHONDONTWRITEBYTECODE=1     PYTHONUNBUFFERED=1

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt     && useradd --system --uid 10001 --no-create-home payguard

COPY main.py .

USER 10001:10001

EXPOSE 8000

HEALTHCHECK --interval=10s --timeout=5s --retries=10 --start-period=15s     CMD python -c "import urllib.request; urllib.request.urlopen('http://127.0.0.1:8000/health', timeout=3)"

CMD ["uvicorn","main:app","--host","0.0.0.0","--port","8000"]
