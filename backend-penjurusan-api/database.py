import os
from sqlalchemy import create_engine
from sqlalchemy.orm import declarative_base, sessionmaker

raw_url = os.getenv("DATABASE_URL", "postgresql://postgres:Smanway2026@db.fjkqpmzljkfesmpqgdxm.supabase.co:6543/postgres")

if "://" in raw_url:
    db_path = raw_url.split("://", 1)[1]
    SQLALCHEMY_DATABASE_URL = f"postgresql+psycopg2://{db_path}"
else:
    SQLALCHEMY_DATABASE_URL = raw_url

engine = create_engine(SQLALCHEMY_DATABASE_URL, pool_pre_ping=True, pool_size=10, max_overflow=20)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()