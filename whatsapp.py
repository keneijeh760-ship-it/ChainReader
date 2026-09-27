import os

import httpx

GRAPH_URL = "https://graph.facebook.com/v21.0"


def parse_incoming(payload: dict) -> list[dict]:
    events = []
    for entry in payload.get("entry", []):
        for change in entry.get("changes", []):
            value = change.get("value") or {}
            phone_number_id = (value.get("metadata") or {}).get("phone_number_id")
            for message in value.get("messages") or []:
                if message.get("type") != "text":
                    continue
                body = (message.get("text") or {}).get("body", "").strip()
                if not body:
                    continue
                events.append(
                    {
                        "phone_number_id": phone_number_id,
                        "from": message.get("from"),
                        "body": body,
                    }
                )
    return events


def send_text(phone_number_id: str, to: str, body: str) -> None:
    response = httpx.post(
        f"{GRAPH_URL}/{phone_number_id}/messages",
        headers={"Authorization": f"Bearer {os.environ['WHATSAPP_TOKEN']}"},
        json={
            "messaging_product": "whatsapp",
            "to": to,
            "type": "text",
            "text": {"body": body},
        },
        timeout=30.0,
    )
    response.raise_for_status()
