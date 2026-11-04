import os
from sqlalchemy import create_engine
from sqlalchemy.orm import declarative_base, sessionmaker

# Ambil URL dari Environment Variables
url = os.getenv("DATABASE_URL", "postgresql://postgres:Smanway2026@db.fjkqpmzljkfesmpqgdxm.supabase.co:6543/postgres")

# Pastikan SQLAlchemy menggunakan psycopg2
if url.startswith("postgres://"):
    url = url.replace("postgres://", "postgresql+psycopg2://", 1)
elif url.startswith("postgresql://") and not url.startswith("postgresql+psycopg2://"):
    url = url.replace("postgresql://", "postgresql+psycopg2://", 1)

engine = create_engine(url, pool_pre_ping=True)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()