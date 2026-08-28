from contextlib import asynccontextmanager

from fastapi import FastAPI


@asynccontextmanager
async def lifespan(_: FastAPI):
    yield


def create_app() -> FastAPI:
    return FastAPI(title="ChainReader API", version="0.1.0", lifespan=lifespan)


app = create_app()
