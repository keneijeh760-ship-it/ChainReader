import os

from groq import Groq

# Groq retired llama-3.3-70b-versatile for developer accounts on 16 Aug 2026.
# Swap this constant (or the client below) when moving to another provider.
MODEL = "openai/gpt-oss-20b"


def generate_reply(
    *,
    business_name: str,
    tone: str,
    working_hours: str,
    escalation_contact: str,
    faqs: list[dict],
    history: list[dict],
    question: str,
) -> str:
    if faqs:
        faq_block = "\n".join(
            f"Q: {item['question']}\nA: {item['answer']}" for item in faqs
        )
        faq_instruction = (
            "Answer only from the FAQ entries below. "
            "If they do not cover the question, do not invent a policy."
        )
    else:
        faq_block = "(none)"
        faq_instruction = (
            "No FAQ entry matched. Do not invent an answer. "
            f"Tell the customer you will pass this to {escalation_contact}."
        )

    system = (
        f"You are the WhatsApp customer service assistant for {business_name}. "
        f"Tone: {tone}. Working hours: {working_hours}. "
        f"Escalation contact: {escalation_contact}. "
        f"{faq_instruction}\n\nFAQ entries:\n{faq_block}"
    )

    messages: list[dict] = [{"role": "system", "content": system}]
    for turn in history:
        role = "assistant" if turn["role"] == "assistant" else "user"
        messages.append({"role": role, "content": turn["body"]})
    messages.append({"role": "user", "content": question})

    client = Groq(api_key=os.environ["GROQ_API_KEY"])
    response = client.chat.completions.create(
        model=MODEL,
        messages=messages,
        temperature=0.3,
    )
    text = response.choices[0].message.content or ""
    return text.strip()
