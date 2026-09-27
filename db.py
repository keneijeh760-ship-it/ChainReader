import os

import psycopg
from pgvector.psycopg import register_vector
from psycopg.rows import dict_row


def get_conn():
    conn = psycopg.connect(os.environ["DATABASE_URL"], row_factory=dict_row)
    register_vector(conn)
    return conn


def init_db() -> None:
    with get_conn() as conn:
        conn.execute("CREATE EXTENSION IF NOT EXISTS vector")
        conn.execute(
            """
            CREATE TABLE IF NOT EXISTS clients (
                client_id TEXT PRIMARY KEY,
                business_name TEXT NOT NULL,
                tone TEXT NOT NULL,
                working_hours TEXT NOT NULL,
                escalation_contact TEXT NOT NULL,
                whatsapp_phone_number_id TEXT UNIQUE NOT NULL
            )
            """
        )
        conn.execute(
            """
            CREATE TABLE IF NOT EXISTS faq_entries (
                id SERIAL PRIMARY KEY,
                client_id TEXT NOT NULL REFERENCES clients(client_id),
                question TEXT NOT NULL,
                answer TEXT NOT NULL,
                embedding vector(384) NOT NULL
            )
            """
        )
        conn.execute(
            """
            CREATE TABLE IF NOT EXISTS messages (
                id SERIAL PRIMARY KEY,
                client_id TEXT NOT NULL REFERENCES clients(client_id),
                wa_from TEXT NOT NULL,
                role TEXT NOT NULL,
                body TEXT NOT NULL,
                created_at TIMESTAMPTZ NOT NULL DEFAULT now()
            )
            """
        )


def add_faq_entry(client_id: str, question: str, answer: str) -> None:
    from rag import embed_text

    embedding = embed_text(question)
    with get_conn() as conn:
        conn.execute(
            """
            INSERT INTO faq_entries (client_id, question, answer, embedding)
            VALUES (%s, %s, %s, %s)
            """,
            (client_id, question, answer, embedding),
        )


def get_client_by_phone(phone_number_id: str) -> dict | None:
    with get_conn() as conn:
        return conn.execute(
            """
            SELECT client_id, business_name, tone, working_hours,
                   escalation_contact, whatsapp_phone_number_id
            FROM clients
            WHERE whatsapp_phone_number_id = %s
            """,
            (phone_number_id,),
        ).fetchone()


def log_message(client_id: str, wa_from: str, role: str, body: str) -> None:
    with get_conn() as conn:
        conn.execute(
            """
            INSERT INTO messages (client_id, wa_from, role, body)
            VALUES (%s, %s, %s, %s)
            """,
            (client_id, wa_from, role, body),
        )


def recent_messages(client_id: str, wa_from: str, limit: int = 8) -> list[dict]:
    with get_conn() as conn:
        rows = conn.execute(
            """
            SELECT role, body
            FROM messages
            WHERE client_id = %s AND wa_from = %s
            ORDER BY created_at DESC
            LIMIT %s
            """,
            (client_id, wa_from, limit),
        ).fetchall()
    return list(reversed(rows))
