from dataclasses import dataclass, field
from datetime import datetime, date
from enum import Enum
from typing import Optional


class Visibility(Enum):
    PUBLIC = "公开"
    FRIENDS_ONLY = "仅好友"
    PRIVATE = "仅自己"


@dataclass
class User:
    id: int
    username: str
    real_name: str
    avatar: str
    school: str
    class_name: str
    gender: str = ""
    birthday: Optional[date] = None
    bio: str = ""


class FriendStatus(Enum):
    PENDING = "pending"
    ACCEPTED = "accepted"
    REJECTED = "rejected"


@dataclass
class FriendRelation:
    id: int
    user_id: int
    friend_id: int
    status: FriendStatus
    created_at: datetime = field(default_factory=datetime.now)


@dataclass
class EditRecord:
    """编辑历史记录"""
    id: int
    post_id: int
    user_id: int
    old_content: str
    new_content: str
    old_visibility: Visibility
    new_visibility: Visibility
    edited_at: datetime = field(default_factory=datetime.now)


@dataclass
class Bookmark:
    """收藏记录"""
    id: int
    user_id: int
    post_id: int
    created_at: datetime = field(default_factory=datetime.now)


@dataclass
class PhotoPost:
    id: int
    user_id: int
    content: str
    image_url: str
    visibility: Visibility
    like_count: int = 0
    created_at: datetime = field(default_factory=datetime.now)
    liked_by: list = field(default_factory=list)
    pinned: bool = False
    edited: bool = False
    last_edited_at: Optional[datetime] = None
    bookmarks: list = field(default_factory=list)  # list of user_ids
    edit_history: list = field(default_factory=list)  # list of EditRecord

    def is_liked_by(self, user_id: int) -> bool:
        return user_id in self.liked_by

    def toggle_like(self, user_id: int):
        if user_id in self.liked_by:
            self.liked_by.remove(user_id)
            self.like_count -= 1
        else:
            self.liked_by.append(user_id)
            self.like_count += 1

    def is_bookmarked_by(self, user_id: int) -> bool:
        return user_id in self.bookmarks

    def toggle_bookmark(self, user_id: int):
        if user_id in self.bookmarks:
            self.bookmarks.remove(user_id)
        else:
            self.bookmarks.append(user_id)

    @property
    def bookmark_count(self) -> int:
        return len(self.bookmarks)


@dataclass
class Comment:
    id: int
    post_id: int
    user_id: int
    content: str
    created_at: datetime = field(default_factory=datetime.now)
    replies: list = field(default_factory=list)


@dataclass
class Reply:
    id: int
    comment_id: int
    user_id: int
    content: str
    created_at: datetime = field(default_factory=datetime.now)
