from sqlalchemy import create_engine
from sqlalchemy.orm import declarative_base
from sqlalchemy.orm import sessionmaker

# Nama file database yang akan terbuat otomatis
SQLALCHEMY_DATABASE_URL = "postgresql://postgres:Smanway2026@db.fjkqpmzljkfesmpqgdxm.supabase.co:5432/postgres"

engine = create_engine(SQLALCHEMY_DATABASE_URL)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()