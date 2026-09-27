import os
from contextlib import asynccontextmanager

from dotenv import load_dotenv
from fastapi import FastAPI, HTTPException, Query, Request
from fastapi.responses import PlainTextResponse

load_dotenv()

from db import get_client_by_phone, init_db, log_message, recent_messages
from llm import generate_reply
from rag import search_faqs
from whatsapp import parse_incoming, send_text


@asynccontextmanager
async def lifespan(_: FastAPI):
    init_db()
    yield


app = FastAPI(title="WhatsApp AI Customer Service", lifespan=lifespan)


@app.get("/webhook")
def verify_webhook(
    mode: str = Query(alias="hub.mode"),
    token: str = Query(alias="hub.verify_token"),
    challenge: str = Query(alias="hub.challenge"),
):
    if mode == "subscribe" and token == os.environ["WHATSAPP_VERIFY_TOKEN"]:
        return PlainTextResponse(challenge)
    raise HTTPException(status_code=403, detail="Verification failed")


@app.post("/webhook")
async def receive_webhook(request: Request):
    payload = await request.json()
    for event in parse_incoming(payload):
        client = get_client_by_phone(event["phone_number_id"])
        if client is None:
            continue
        history = recent_messages(client["client_id"], event["from"])
        faqs = search_faqs(client["client_id"], event["body"])
        reply = generate_reply(
            business_name=client["business_name"],
            tone=client["tone"],
            working_hours=client["working_hours"],
            escalation_contact=client["escalation_contact"],
            faqs=faqs,
            history=history,
            question=event["body"],
        )
        send_text(event["phone_number_id"], event["from"], reply)
        log_message(client["client_id"], event["from"], "user", event["body"])
        log_message(client["client_id"], event["from"], "assistant", reply)
    return {"status": "ok"}
