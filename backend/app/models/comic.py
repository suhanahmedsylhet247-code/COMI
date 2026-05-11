import datetime

from sqlalchemy import Column, DateTime, ForeignKey, Integer, String, Table, Text
from sqlalchemy.orm import DeclarativeBase, relationship


class Base(DeclarativeBase):
    pass


comic_tags = Table(
    "comic_tags",
    Base.metadata,
    Column("comic_id", Integer, ForeignKey("comics.id"), primary_key=True),
    Column("tag_id", Integer, ForeignKey("tags.id"), primary_key=True),
)


class Comic(Base):
    __tablename__ = "comics"

    id = Column(Integer, primary_key=True, autoincrement=True)
    title = Column(String(500), nullable=False, index=True)
    file_path = Column(String(1000), nullable=False, unique=True)
    file_format = Column(String(10), nullable=False)
    file_size = Column(Integer, nullable=False)
    page_count = Column(Integer, nullable=False, default=0)
    cover_path = Column(String(1000), nullable=True)
    author = Column(String(300), nullable=True)
    series = Column(String(500), nullable=True)
    volume = Column(Integer, nullable=True)
    description = Column(Text, nullable=True)
    publisher = Column(String(300), nullable=True)
    year = Column(Integer, nullable=True)
    added_at = Column(DateTime, default=datetime.datetime.utcnow)
    updated_at = Column(
        DateTime, default=datetime.datetime.utcnow, onupdate=datetime.datetime.utcnow
    )

    tags = relationship("Tag", secondary=comic_tags, back_populates="comics", lazy="selectin")
    progress = relationship(
        "ReadingProgress", back_populates="comic", uselist=False, lazy="selectin"
    )
    bookmarks = relationship("Bookmark", back_populates="comic", lazy="selectin")


class Tag(Base):
    __tablename__ = "tags"

    id = Column(Integer, primary_key=True, autoincrement=True)
    name = Column(String(100), nullable=False, unique=True, index=True)

    comics = relationship("Comic", secondary=comic_tags, back_populates="tags", lazy="selectin")
