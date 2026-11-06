import os
from dotenv import load_dotenv
from sqlalchemy import create_engine
from sqlalchemy.orm import declarative_base, sessionmaker

load_dotenv()

# Gunakan string koneksi yang menyertakan parameter sslmode=require
SQLALCHEMY_DATABASE_URL = (
    "postgresql+psycopg2://postgres:Smanway2026@aws-0-ap-southeast-1.pooler.supabase.com:6543/postgres"
)

# Tambahkan connect_args untuk mengaktifkan SSL yang diminta Supabase
engine = create_engine(
    SQLALCHEMY_DATABASE_URL,
    connect_args={"sslmode": "require"},
    pool_pre_ping=True
)

SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()