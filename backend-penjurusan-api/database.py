import os
from sqlalchemy import create_engine
from sqlalchemy.orm import declarative_base, sessionmaker

# Ambil URL dari Environment Variables
url = os.getenv("DATABASE_URL", "postgresql://postgres:Smanway2026@db.fjkqpmzljkfesmpqgdxm.supabase.co:6543/postgres")

# Ubah prefix agar cocok dengan psycopg versi 3
if url.startswith("postgres://"):
    url = url.replace("postgres://", "postgresql+psycopg://", 1)
elif url.startswith("postgresql://") and not url.startswith("postgresql+psycopg://"):
    url = url.replace("postgresql://", "postgresql+psycopg://", 1)

engine = create_engine(url, pool_pre_ping=True)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()