"""模拟数据存储层 —— 与 Java 端数据结构对齐，额外增加收藏和编辑历史"""

from datetime import datetime, date
from models import (
    User, PhotoPost, Comment, Reply, FriendRelation,
    EditRecord, Visibility, FriendStatus,
)

# ========== 用户 ==========
USERS: dict[int, User] = {}
next_user_id = 1

# ========== 帖子 ==========
POSTS: dict[int, PhotoPost] = {}
next_post_id = 1

# ========== 评论 ==========
COMMENTS: dict[int, Comment] = {}
next_comment_id = 1
next_reply_id = 1

# ========== 好友关系 ==========
RELATIONS: dict[int, FriendRelation] = {}
next_relation_id = 1

# ========== 编辑历史 ==========
EDIT_HISTORY: dict[int, EditRecord] = {}
next_edit_id = 1


def init_mock_data():
    global next_user_id, next_post_id, next_comment_id, next_reply_id, next_relation_id

    # 用户
    _add_user(User(1, "liming", "李明", "/images/avatar1.svg",
                   "北京大学", "计算机2022级1班", "男", date(2003, 5, 12), "热爱编程，喜欢打篮球"))
    _add_user(User(2, "wangfang", "王芳", "/images/avatar2.svg",
                   "北京大学", "计算机2022级1班", "女", date(2003, 8, 23), "读书和旅行是我最大的爱好"))
    _add_user(User(3, "zhangwei", "张伟", "/images/avatar3.svg",
                   "清华大学", "软件工程2022级2班", "男", date(2003, 3, 15), "代码改变世界"))
    _add_user(User(4, "liuna", "刘娜", "/images/avatar4.svg",
                   "北京大学", "计算机2022级2班", "女", date(2003, 11, 8), "生活需要仪式感"))
    _add_user(User(5, "chenjie", "陈杰", "/images/avatar5.svg",
                   "北京大学", "计算机2022级1班", "男", date(2003, 7, 20), "羽毛球爱好者"))
    _add_user(User(6, "zhaoyang", "赵阳", "/images/avatar6.svg",
                   "清华大学", "软件工程2022级2班", "男", date(2003, 1, 30), "摄影与骑行"))

    # 好友关系
    _add_relation(FriendRelation(1, 1, 2, FriendStatus.ACCEPTED, datetime(2024, 3, 15, 10, 0)))
    _add_relation(FriendRelation(2, 1, 5, FriendStatus.ACCEPTED, datetime(2024, 3, 20, 14, 30)))
    _add_relation(FriendRelation(3, 4, 1, FriendStatus.ACCEPTED, datetime(2024, 4, 5, 9, 15)))
    _add_relation(FriendRelation(4, 2, 4, FriendStatus.ACCEPTED, datetime(2024, 4, 10, 16, 0)))
    _add_relation(FriendRelation(5, 5, 2, FriendStatus.ACCEPTED, datetime(2024, 5, 1, 11, 30)))
    _add_relation(FriendRelation(6, 3, 1, FriendStatus.PENDING, datetime(2024, 6, 10, 8, 45)))
    _add_relation(FriendRelation(7, 4, 5, FriendStatus.PENDING, datetime(2024, 6, 12, 13, 20)))
    _add_relation(FriendRelation(8, 3, 6, FriendStatus.ACCEPTED, datetime(2024, 2, 14, 10, 0)))

    # 帖子
    _add_post(PhotoPost(1, 2, "今天校园的樱花开了，好美！",
                        "/images/photo1.svg", Visibility.PUBLIC, 5,
                        datetime(2024, 6, 15, 9, 30), [1, 5, 3]))
    _add_post(PhotoPost(2, 1, "和同学们一起做项目，收获满满！",
                        "/images/photo2.svg", Visibility.FRIENDS_ONLY, 3,
                        datetime(2024, 6, 14, 15, 20), [2, 5]))
    _add_post(PhotoPost(3, 3, "清华园的秋天，每一步都是风景",
                        "/images/photo3.svg", Visibility.PUBLIC, 8,
                        datetime(2024, 6, 13, 10, 45), [1, 2, 4, 5, 6, 3]))
    _add_post(PhotoPost(4, 4, "周末烘焙课的成果，满满的成就感！",
                        "/images/photo4.svg", Visibility.FRIENDS_ONLY, 6,
                        datetime(2024, 6, 12, 18, 0), [1, 2, 5]))
    _add_post(PhotoPost(5, 1, "今天心情不太好，写点日记...",
                        "/images/photo5.svg", Visibility.PRIVATE, 0,
                        datetime(2024, 6, 11, 22, 30)))
    _add_post(PhotoPost(6, 5, "羽毛球比赛拿了第二名！继续加油！",
                        "/images/photo6.svg", Visibility.PUBLIC, 12,
                        datetime(2024, 6, 10, 16, 45), [1, 2, 3, 4, 6]))
    _add_post(PhotoPost(7, 6, "骑行到长城，风光无限好",
                        "/images/photo7.svg", Visibility.PUBLIC, 4,
                        datetime(2024, 6, 9, 8, 0), [3]))
    _add_post(PhotoPost(8, 2, "和室友的下午茶时光~",
                        "/images/photo8.svg", Visibility.FRIENDS_ONLY, 7,
                        datetime(2024, 6, 8, 14, 30), [1, 4, 5]))

    # 收藏
    POSTS[1].bookmarks = [1, 5]
    POSTS[6].bookmarks = [1, 2, 4]

    # 评论
    _add_comment(Comment(1, 1, 1, "好漂亮！下次带我一起去看",
                         datetime(2024, 6, 15, 10, 0)))
    COMMENTS[1].replies.append(Reply(1, 1, 2, "好呀，约起！",
                                      datetime(2024, 6, 15, 10, 15)))

    _add_comment(Comment(2, 1, 5, "我也想去！",
                         datetime(2024, 6, 15, 11, 30)))

    _add_comment(Comment(3, 2, 2, "李明同学棒棒的！",
                         datetime(2024, 6, 14, 16, 0)))
    COMMENTS[3].replies.append(Reply(2, 3, 1, "谢谢王芳鼓励！",
                                      datetime(2024, 6, 14, 16, 20)))

    _add_comment(Comment(4, 6, 1, "陈杰太厉害了！下次教教我",
                         datetime(2024, 6, 10, 17, 0)))
    COMMENTS[4].replies.append(Reply(3, 4, 5, "没问题，随时约球！",
                                      datetime(2024, 6, 10, 17, 30)))

    _add_comment(Comment(5, 6, 2, "运动健将！",
                         datetime(2024, 6, 10, 18, 0)))

    _add_comment(Comment(6, 3, 1, "清华园真美，有机会去逛逛",
                         datetime(2024, 6, 13, 12, 0)))

    _add_comment(Comment(7, 3, 6, "我每天路过，确实很美",
                         datetime(2024, 6, 13, 13, 0)))


def _add_user(u: User):
    global next_user_id
    USERS[u.id] = u
    next_user_id = max(next_user_id, u.id + 1)


def _add_post(p: PhotoPost):
    global next_post_id
    POSTS[p.id] = p
    next_post_id = max(next_post_id, p.id + 1)


def _add_comment(c: Comment):
    global next_comment_id, next_reply_id
    COMMENTS[c.id] = c
    next_comment_id = max(next_comment_id, c.id + 1)
    for r in c.replies:
        next_reply_id = max(next_reply_id, r.id + 1)


def _add_relation(r: FriendRelation):
    global next_relation_id
    RELATIONS[r.id] = r
    next_relation_id = max(next_relation_id, r.id + 1)


# ========== 查询辅助 ==========

def get_user(uid: int) -> User | None:
    return USERS.get(uid)


def get_post(pid: int) -> PhotoPost | None:
    return POSTS.get(pid)


def are_friends(uid1: int, uid2: int) -> bool:
    return any(
        r.status == FriendStatus.ACCEPTED and
        (r.user_id == uid1 and r.friend_id == uid2 or
         r.user_id == uid2 and r.friend_id == uid1)
        for r in RELATIONS.values()
    )


def is_post_visible(post: PhotoPost, viewer_id: int) -> bool:
    if post.visibility == Visibility.PUBLIC:
        return True
    if post.visibility == Visibility.PRIVATE:
        return post.user_id == viewer_id
    if post.visibility == Visibility.FRIENDS_ONLY:
        return post.user_id == viewer_id or are_friends(post.user_id, viewer_id)
    return False


def get_friends(uid: int) -> list[User]:
    ids = []
    for r in RELATIONS.values():
        if r.status == FriendStatus.ACCEPTED:
            if r.user_id == uid:
                ids.append(r.friend_id)
            elif r.friend_id == uid:
                ids.append(r.user_id)
    return [USERS[i] for i in ids if i in USERS]


def get_pending_received(uid: int) -> list[FriendRelation]:
    return [r for r in RELATIONS.values()
            if r.friend_id == uid and r.status == FriendStatus.PENDING]


def get_comments_for_post(pid: int) -> list[Comment]:
    return sorted(
        [c for c in COMMENTS.values() if c.post_id == pid],
        key=lambda c: c.created_at,
    )


def get_visible_posts(viewer_id: int) -> list[PhotoPost]:
    posts = [p for p in POSTS.values() if is_post_visible(p, viewer_id)]
    posts.sort(key=lambda p: p.created_at, reverse=True)
    return posts


def get_pinned_posts(viewer_id: int) -> list[PhotoPost]:
    posts = [p for p in POSTS.values() if p.pinned and is_post_visible(p, viewer_id)]
    posts.sort(key=lambda p: p.created_at, reverse=True)
    return posts


def get_bookmarked_posts(viewer_id: int) -> list[PhotoPost]:
    return [p for p in POSTS.values() if viewer_id in p.bookmarks]


def get_edit_history(pid: int) -> list[EditRecord]:
    return sorted(
        [e for e in EDIT_HISTORY.values() if e.post_id == pid],
        key=lambda e: e.edited_at,
    )
