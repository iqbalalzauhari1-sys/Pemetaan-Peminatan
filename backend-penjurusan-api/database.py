import os
from sqlalchemy import create_engine
from sqlalchemy.orm import declarative_base, sessionmaker

# 1. Ambil URL (dari Vercel Environment Variables atau default)
raw_url = os.getenv("DATABASE_URL", "postgresql://postgres:Smanway2026@db.fjkqpmzljkfesmpqgdxm.supabase.co:6543/postgres")

# 2. Buang awalan apa pun (postgres://, postgresql://, dll) dan paksa pakai psycopg2
if "://" in raw_url:
    # Ambil semua teks SETELAH '://'
    db_path = raw_url.split("://", 1)[1]
    # Bangun ulang URL secara absolut
    SQLALCHEMY_DATABASE_URL = f"postgresql+psycopg2://{db_path}"
else:
    SQLALCHEMY_DATABASE_URL = raw_url

# 3. Buat Engine
engine = create_engine(SQLALCHEMY_DATABASE_URL, pool_pre_ping=True)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()