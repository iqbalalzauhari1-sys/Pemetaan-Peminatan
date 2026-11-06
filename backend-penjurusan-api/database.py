import os
from dotenv import load_dotenv  # <-- Pastikan baris ini ada
from sqlalchemy import create_engine
from sqlalchemy.orm import declarative_base, sessionmaker

# 1. Panggil load_dotenv dengan huruf kecil semua (l-o-a-d)
load_dotenv()

# 2. Ambil variabel dari .env
SQLALCHEMY_DATABASE_URL = os.getenv("DATABASE_URL")

if not SQLALCHEMY_DATABASE_URL:
  raise ValueError(
      "DATABASE_URL belum diatur di dalam file .env atau environment variable!"
  )

engine = create_engine(SQLALCHEMY_DATABASE_URL, pool_pre_ping=True)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()