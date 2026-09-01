from sqlalchemy import create_engine
from sqlalchemy.orm import declarative_base
from sqlalchemy.orm import sessionmaker

# Nama file database yang akan terbuat otomatis
SQLALCHEMY_DATABASE_URL = "postgresql://postgres:[YOUR-PASSWORD]@db.zdgxrybrjtqqpmpxesvo.supabase.co:5432/postgres"

engine = create_engine(
    SQLALCHEMY_DATABASE_URL, connect_args={"check_same_thread": False}
)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()