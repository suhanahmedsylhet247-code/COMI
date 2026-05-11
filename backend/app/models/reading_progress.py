import datetime

from sqlalchemy import Column, DateTime, Float, ForeignKey, Integer, String, Text
from sqlalchemy.orm import relationship

from app.models.comic import Base


class ReadingProgress(Base):
    __tablename__ = "reading_progress"

    id = Column(Integer, primary_key=True, autoincrement=True)
    comic_id = Column(Integer, ForeignKey("comics.id"), nullable=False, unique=True)
    current_page = Column(Integer, nullable=False, default=0)
    total_pages = Column(Integer, nullable=False, default=0)
    percentage = Column(Float, nullable=False, default=0.0)
    last_read_at = Column(DateTime, default=datetime.datetime.utcnow)
    started_at = Column(DateTime, default=datetime.datetime.utcnow)
    finished_at = Column(DateTime, nullable=True)

    comic = relationship("Comic", back_populates="progress")


class Bookmark(Base):
    __tablename__ = "bookmarks"

    id = Column(Integer, primary_key=True, autoincrement=True)
    comic_id = Column(Integer, ForeignKey("comics.id"), nullable=False)
    page_number = Column(Integer, nullable=False)
    label = Column(String(200), nullable=True)
    note = Column(Text, nullable=True)
    created_at = Column(DateTime, default=datetime.datetime.utcnow)

    comic = relationship("Comic", back_populates="bookmarks")
