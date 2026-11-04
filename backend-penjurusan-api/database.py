import os
from sqlalchemy import create_engine
from sqlalchemy.orm import declarative_base
from sqlalchemy.orm import sessionmaker

# 1. Ambil URL mentah dari Vercel Environment Variables
raw_url = os.getenv("DATABASE_URL", "")

if not raw_url:
    # Fallback jika kosong
    SQLALCHEMY_DATABASE_URL = "postgresql+psycopg2://postgres:Smanway2026@db.fjkqpmzljkfesmpqgdxm.supabase.co:6543/postgres"
else:
    # 2. Bersihkan string dari prefix yang salah atau ketinggalan zaman, lalu paksa jadi +psycopg2
    clean_url = raw_url.strip()
    
    if "://" in clean_url:
        # Ambil bagian setelah '://' (user:password@host:port/dbname)
        user_pass_host = clean_url.split("://", 1)[1]
        SQLALCHEMY_DATABASE_URL = f"postgresql+psycopg2://{user_pass_host}"
    else:
        # Jaga-jaga jika isinya hanya host/ip
        SQLALCHEMY_DATABASE_URL = f"postgresql+psycopg2://{clean_url}"

# Buat engine dengan konfigurasi aman
engine = create_engine(SQLALCHEMY_DATABASE_URL, pool_pre_ping=True)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()