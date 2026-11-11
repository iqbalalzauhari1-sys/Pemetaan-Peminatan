import os
from sqlalchemy import create_engine
from sqlalchemy.orm import declarative_base, sessionmaker

# Kita tulis mati URL-nya di sini dengan format Pooler IPv4 yang benar
# Pastikan ada .xdwlwlfdfkiedpntvglz setelah postgres!
SQLALCHEMY_DATABASE_URL = "postgresql://postgres.xdwlwlfdfkiedpntvglz:Smanway2026@aws-0-ap-southeast-1.pooler.supabase.com:6543/postgres?sslmode=require"

engine = create_engine(
    SQLALCHEMY_DATABASE_URL,
    pool_pre_ping=True
)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()