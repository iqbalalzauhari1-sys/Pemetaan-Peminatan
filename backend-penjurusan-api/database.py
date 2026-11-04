import os
from sqlalchemy import create_engine
from sqlalchemy.orm import declarative_base
from sqlalchemy.orm import sessionmaker

# Mengambil URL dari Environment Vercel, fallback ke lokal jika sedang dijalankan offline
SQLALCHEMY_DATABASE_URL = os.getenv(
    "postgresql+psycopg2://postgres:Smanway2026@db.fjkqpmzljkfesmpqgdxm.supabase.co:5432/postgres"
)

# Tambahkan argumen pool_pre_ping untuk koneksi serverless yang stabil
engine = create_engine(SQLALCHEMY_DATABASE_URL, pool_pre_ping=True)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()