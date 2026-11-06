import os
from dotenv import load_dotenv
from sqlalchemy import create_engine
from sqlalchemy.orm import declarative_base, sessionmaker

# Memuat variabel dari file .env
load_dotenv()

SQLALCHEMY_DATABASE_URL = os.getenv("DATABASE_URL")

# Validasi pengaman jika URL kosong
if not SQLALCHEMY_DATABASE_URL:
    raise ValueError(
        "DATABASE_URL belum diatur di dalam file .env atau environment variable!"
    )

engine = create_engine(SQLALCHEMY_DATABASE_URL, pool_pre_ping=True)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()