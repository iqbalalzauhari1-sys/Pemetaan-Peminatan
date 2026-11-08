from sqlalchemy import create_engine
from sqlalchemy.orm import declarative_base, sessionmaker

# URL Database Supabase-mu (menggunakan port 5432 langsung)
SQLALCHEMY_DATABASE_URL = (
    "postgresql://postgres:Smanway2026@db.xdwlwlfdfkiedpntvglz.supabase.co:5432/postgres"
)

# Wajib gunakan sslmode=require untuk Supabase
engine = create_engine(
    SQLALCHEMY_DATABASE_URL,
    connect_args={"sslmode": "require"},
    pool_pre_ping=True
)

SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()