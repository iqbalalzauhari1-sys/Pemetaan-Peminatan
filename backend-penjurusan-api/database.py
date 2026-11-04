import os
from sqlalchemy import create_engine
from sqlalchemy.orm import declarative_base
from sqlalchemy.orm import sessionmaker

# Ambil URL dari Environment Variable Vercel
SQLALCHEMY_DATABASE_URL = os.getenv("DATABASE_URL", "")

# Jika kosong, gunakan fallback default
if not SQLALCHEMY_DATABASE_URL:
    SQLALCHEMY_DATABASE_URL = "postgresql://postgres:Smanway2026@db.fjkqpmzljkfesmpqgdxm.supabase.co:5432/postgres"

# Paksa ganti dialect agar selalu menggunakan psycopg2 (mencegah error pencarian modul 'psycopg' v3)
if SQLALCHEMY_DATABASE_URL.startswith("postgres://"):
    SQLALCHEMY_DATABASE_URL = SQLALCHEMY_DATABASE_URL.replace("postgres://", "postgresql+psycopg2://", 1)
elif SQLALCHEMY_DATABASE_URL.startswith("postgresql://"):
    SQLALCHEMY_DATABASE_URL = SQLALCHEMY_DATABASE_URL.replace("postgresql://", "postgresql+psycopg2://", 1)
elif not "psycopg2" in SQLALCHEMY_DATABASE_URL:
    # Jaga-jaga jika formatnya langsung domain
    SQLALCHEMY_DATABASE_URL = f"postgresql+psycopg2://{SQLALCHEMY_DATABASE_URL.split('://')[-1]}"

engine = create_engine(SQLALCHEMY_DATABASE_URL, pool_pre_ping=True)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()