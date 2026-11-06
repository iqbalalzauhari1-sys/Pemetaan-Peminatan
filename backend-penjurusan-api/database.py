import os
from dotenv import load_dotenv
from sqlalchemy import create_engine
from sqlalchemy.orm import declarative_base, sessionmaker

load_dotenv()

# Masukkan URL database secara langsung ke sini agar tidak terbaca kosong
SQLALCHEMY_DATABASE_URL = (
    "postgresql://postgres.xdwlwlfdfkiedpntvglz:Smanway2026@aws-0-ap-southeast-1.pooler.supabase.com:6543/postgres"
)

engine = create_engine(SQLALCHEMY_DATABASE_URL, pool_pre_ping=True)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()