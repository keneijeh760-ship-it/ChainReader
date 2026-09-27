# WhatsApp AI Customer Service Bot

One FastAPI service and one Postgres schema. A new client is a database row plus their FAQ entries — no new deployment.

FAQ search uses a local embedding model, so ingest and retrieval do not call a paid API. Replies are written by Groq. Swap providers by editing `llm.py` only.

## Setup (local, ~20 min)

1. `pip install -r requirements.txt`
2. Copy `.env.example` to `.env`, fill in:
   - `GROQ_API_KEY` — from [console.groq.com](https://console.groq.com)
   - `WHATSAPP_TOKEN` + `WHATSAPP_PHONE_NUMBER_ID` — from a Meta developer app
     ([developers.facebook.com](https://developers.facebook.com) → create app → add WhatsApp product → test number is free)
   - `WHATSAPP_VERIFY_TOKEN` — make up any string, you'll enter it in the Meta dashboard
   - `DATABASE_URL` — either run Postgres locally in Docker:
     `docker run -d -p 5432:5432 -e POSTGRES_PASSWORD=pass ankane/pgvector`
     (ankane/pgvector image has the vector extension pre-installed)
     or use a free hosted Postgres (Neon.tech or Supabase both have pgvector-enabled free tiers)
3. `uvicorn main:app --reload`
4. In a second terminal: `ngrok http 8000` (free) to get a public URL for Meta to hit
5. In the Meta app dashboard, set the webhook URL to `https://<ngrok-url>/webhook`
   and the verify token to whatever you put in `.env`

The first FAQ insert or incoming message downloads `all-MiniLM-L6-v2` (about 90 MB) into the local Hugging Face cache.

Groq retired `llama-3.3-70b-versatile` for developer accounts on 16 August 2026. `llm.py` calls `openai/gpt-oss-20b`, the cheapest current production chat model on Groq. Change the `MODEL` constant in that file to use a different one.

## Onboarding a new client

```python
from db import get_conn, add_faq_entry

with get_conn() as conn:
    conn.execute(
        "INSERT INTO clients (client_id, business_name, tone, working_hours, "
        "escalation_contact, whatsapp_phone_number_id) VALUES (%s,%s,%s,%s,%s,%s)",
        ("client_001", "Client's Business Name", "warm and casual",
         "8am-8pm WAT", "+234...", "their_whatsapp_phone_number_id")
    )

add_faq_entry("client_001", "What are your delivery times?",
              "We deliver within Lagos in 24-48 hours, other states 3-5 days.")
add_faq_entry("client_001", "Do you accept returns?",
              "Yes, within 7 days if the item is unused and in original packaging.")
```

Each client just needs: a row in `clients`, and their FAQ ingested via `add_faq_entry`. No code changes, no new deployment.

`whatsapp_phone_number_id` must match the phone number id Meta sends on the webhook. Incoming text is embedded, matched against that client's FAQ with pgvector, then answered in the configured tone. If nothing is close enough, the model is told to hand the question to `escalation_contact` instead of inventing a policy. Turns are stored in `messages`.

## What's deliberately NOT in v1

- No admin UI — update FAQ via the script above or a quick CLI
- No Redis — Postgres handles the conversation log fine at low volume
- No Kafka — add only past ~3 concurrent high-traffic clients
- No auth on the webhook beyond Meta's own verify token — fine until you're handling anything sensitive enough to need it
- No retry/rate-limit handling — Groq will 429 if you hit limits; catch it and reply "let me get back to you" as a fallback later

## Swapping to another LLM

Edit `llm.py` only — swap the Groq client for Anthropic or OpenAI's SDK, same `generate_reply(...)` signature, nothing else in the app changes.

## Layout

| File | Role |
|---|---|
| `main.py` | FastAPI app, `GET`/`POST /webhook` |
| `db.py` | Connection, schema, `add_faq_entry`, conversation log |
| `rag.py` | Local embeddings and pgvector search |
| `llm.py` | Reply generation |
| `whatsapp.py` | Parse webhook payloads and send replies |
