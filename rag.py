from db import get_conn

# Cosine distance on normalized MiniLM vectors. 0 is identical.
MAX_DISTANCE = 0.55

_model = None


def _get_model():
    global _model
    if _model is None:
        from sentence_transformers import SentenceTransformer

        _model = SentenceTransformer("all-MiniLM-L6-v2")
    return _model


def embed_text(text: str) -> list[float]:
    vector = _get_model().encode(text, normalize_embeddings=True)
    return vector.tolist()


def search_faqs(client_id: str, question: str, limit: int = 3) -> list[dict]:
    embedding = embed_text(question)
    with get_conn() as conn:
        rows = conn.execute(
            """
            SELECT question, answer, (embedding <=> %s) AS distance
            FROM faq_entries
            WHERE client_id = %s
            ORDER BY embedding <=> %s
            LIMIT %s
            """,
            (embedding, client_id, embedding, limit),
        ).fetchall()
    return [
        {"question": row["question"], "answer": row["answer"]}
        for row in rows
        if row["distance"] <= MAX_DISTANCE
    ]
