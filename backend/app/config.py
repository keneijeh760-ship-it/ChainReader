from functools import lru_cache

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")

    etherscan_api_key: str = ""
    etherscan_base_url: str = "https://api.etherscan.io/api"
    database_url: str = "postgresql+asyncpg://chainreader:chainreader@localhost:5432/chainreader"
    cors_origins: str = "http://localhost:5173"
    cache_ttl_seconds: int = 900

    @property
    def cors_origin_list(self) -> list[str]:
        return [origin.strip() for origin in self.cors_origins.split(",") if origin.strip()]


@lru_cache
def get_settings() -> Settings:
    return Settings()
