"""校内网社交原型 - Python Flask 增强详情视图"""

import os
import sys

# 确保 import 路径正确
sys.path.insert(0, os.path.dirname(__file__))

from flask import Flask, render_template, request, redirect, url_for, session, flash
from models import Visibility, EditRecord
from store import (
    init_mock_data, USERS, POSTS, COMMENTS, EDIT_HISTORY,
    next_comment_id, next_reply_id, next_edit_id,
    get_user, get_post, are_friends, is_post_visible,
    get_friends, get_pending_received, get_comments_for_post,
    get_visible_posts, get_pinned_posts, get_bookmarked_posts,
    get_edit_history,
)

app = Flask(__name__)
app.secret_key = "campus-social-prototype"

# 初始化模拟数据
init_mock_data()


def current_user_id() -> int:
    return session.get("user_id", 1)


def current_user():
    return get_user(current_user_id())


# ========== 首页（简化版动态广场，链接到 Java 端） ==========

@app.route("/")
def index():
    uid = current_user_id()
    user = current_user()
    posts = get_visible_posts(uid)
    pinned = get_pinned_posts(uid)
    all_users = list(USERS.values())
    friends = get_friends(uid)
    pending = get_pending_received(uid)
    bookmarked = get_bookmarked_posts(uid)

    return render_template("index.html",
                           user=user, posts=posts, pinned=pinned,
                           all_users=all_users, users_map=USERS,
                           friends=friends, pending=pending,
                           bookmarked_count=len(bookmarked),
                           friend_count=len(friends),
                           pending_count=len(pending),
                           visible_count=len(posts),
                           post_count=len(POSTS),
                           java_port=8080)


@app.post("/switch-user")
def switch_user():
    uid = request.form.get("user_id", type=int)
    if uid and uid in USERS:
        session["user_id"] = uid
    return redirect(url_for("index"))


# ========== 增强详情视图 ==========

@app.route("/post/<int:pid>")
def post_detail(pid):
    uid = current_user_id()
    user = current_user()
    post = get_post(pid)
    if not post:
        flash("该动态不存在或已被删除", "error")
        return redirect(url_for("index"))
    if not is_post_visible(post, uid):
        flash("无权查看该动态", "error")
        return redirect(url_for("index"))

    author = get_user(post.user_id)
    comments = get_comments_for_post(pid)
    is_owner = post.user_id == uid
    edit_history = get_edit_history(pid)
    friends = get_friends(uid)
    bookmarked = get_bookmarked_posts(uid)

    # 评论筛选
    comment_filter = request.args.get("filter", "all")
    if comment_filter == "mine":
        comments = [c for c in comments if c.user_id == uid]
    elif comment_filter == "with_replies":
        comments = [c for c in comments if c.replies]

    return render_template("detail.html",
                           post=post, author=author, user=user,
                           comments=comments, is_owner=is_owner,
                           users_map=USERS, visibilities=list(Visibility),
                           edit_history=edit_history,
                           comment_filter=comment_filter,
                           friend_count=len(friends),
                           bookmarked_count=len(bookmarked),
                           pending_count=len(get_pending_received(uid)),
                           visible_count=len(get_visible_posts(uid)),
                           post_count=len(POSTS),
                           java_port=8080)


@app.post("/post/<int:pid>/like")
def like_post(pid):
    post = get_post(pid)
    uid = current_user_id()
    if post and is_post_visible(post, uid):
        post.toggle_like(uid)
    return redirect(url_for("post_detail", pid=pid))


@app.post("/post/<int:pid>/bookmark")
def bookmark_post(pid):
    post = get_post(pid)
    uid = current_user_id()
    if post and is_post_visible(post, uid):
        post.toggle_bookmark(uid)
        if uid in post.bookmarks:
            flash("已收藏", "success")
        else:
            flash("已取消收藏", "success")
    return redirect(url_for("post_detail", pid=pid))


@app.post("/post/<int:pid>/comment")
def add_comment(pid):
    uid = current_user_id()
    content = request.form.get("content", "").strip()
    if not content:
        flash("评论内容不能为空", "error")
        return redirect(url_for("post_detail", pid=pid))

    global next_comment_id
    from models import Comment
    from datetime import datetime
    c = Comment(next_comment_id, pid, uid, content, datetime.now())
    COMMENTS[c.id] = c
    next_comment_id += 1
    flash("评论成功", "success")
    return redirect(url_for("post_detail", pid=pid))


@app.post("/comment/<int:cid>/reply")
def add_reply(cid):
    uid = current_user_id()
    content = request.form.get("content", "").strip()
    post_id = request.form.get("post_id", type=int)
    if not content:
        flash("回复内容不能为空", "error")
        return redirect(url_for("post_detail", pid=post_id))

    comment = COMMENTS.get(cid)
    if comment:
        global next_reply_id
        from models import Reply
        from datetime import datetime
        r = Reply(next_reply_id, cid, uid, content, datetime.now())
        comment.replies.append(r)
        next_reply_id += 1
        flash("回复成功", "success")
    return redirect(url_for("post_detail", pid=post_id))


@app.post("/post/<int:pid>/edit")
def edit_post(pid):
    uid = current_user_id()
    post = get_post(pid)
    if not post or post.user_id != uid:
        flash("无权编辑该动态", "error")
        return redirect(url_for("post_detail", pid=pid))

    content = request.form.get("content", "").strip()
    visibility = request.form.get("visibility", "PUBLIC")
    if not content:
        flash("内容不能为空", "error")
        return redirect(url_for("post_detail", pid=pid))
    if len(content) > 500:
        flash("内容不能超过500字", "error")
        return redirect(url_for("post_detail", pid=pid))

    # 记录编辑历史
    global next_edit_id
    from datetime import datetime
    old_vis = post.visibility
    new_vis = Visibility[visibility]
    record = EditRecord(
        next_edit_id, pid, uid,
        post.content, content,
        old_vis, new_vis,
        datetime.now(),
    )
    EDIT_HISTORY[record.id] = record
    next_edit_id += 1

    post.content = content
    post.visibility = new_vis
    post.edited = True
    post.last_edited_at = datetime.now()
    flash("编辑成功", "success")
    return redirect(url_for("post_detail", pid=pid))


@app.post("/post/<int:pid>/pin")
def pin_post(pid):
    uid = current_user_id()
    post = get_post(pid)
    if not post or post.user_id != uid:
        flash("无权操作", "error")
        return redirect(url_for("post_detail", pid=pid))
    post.pinned = not post.pinned
    flash("已置顶" if post.pinned else "已取消置顶", "success")
    return redirect(url_for("post_detail", pid=pid))


@app.post("/post/<int:pid>/delete")
def delete_post(pid):
    uid = current_user_id()
    post = get_post(pid)
    if not post or post.user_id != uid:
        flash("无权操作", "error")
        return redirect(url_for("post_detail", pid=pid))
    # 级联删除评论
    to_del = [cid for cid, c in COMMENTS.items() if c.post_id == pid]
    for cid in to_del:
        del COMMENTS[cid]
    del POSTS[pid]
    flash("动态已删除", "success")
    return redirect(url_for("index"))


@app.post("/comment/<int:cid>/delete")
def delete_comment(cid):
    uid = current_user_id()
    comment = COMMENTS.get(cid)
    if not comment or comment.user_id != uid:
        flash("无权删除", "error")
        return redirect(url_for("post_detail", pid=request.form.get("post_id", type=int)))
    del COMMENTS[cid]
    flash("评论已删除", "success")
    return redirect(url_for("post_detail", pid=request.form.get("post_id", type=int)))


@app.post("/comments/batch-delete")
def batch_delete_comments():
    uid = current_user_id()
    post_id = request.form.get("post_id", type=int)
    ids_str = request.form.get("comment_ids", "")
    if not ids_str:
        flash("未选择任何评论", "error")
        return redirect(url_for("post_detail", pid=post_id))

    ids = [int(x) for x in ids_str.split(",") if x.strip()]
    deleted = 0
    for cid in ids:
        c = COMMENTS.get(cid)
        if c and c.user_id == uid:
            del COMMENTS[cid]
            deleted += 1

    if deleted:
        flash(f"已删除 {deleted} 条评论", "success")
    else:
        flash("未删除任何评论（只能删除自己的）", "error")
    return redirect(url_for("post_detail", pid=post_id))


@app.post("/friend/request/<int:to_uid>")
def send_friend_request(to_uid):
    uid = current_user_id()
    if uid == to_uid:
        return redirect(url_for("index"))
    from store import RELATIONS, next_relation_id
    from models import FriendRelation, FriendStatus
    from datetime import datetime
    # 检查是否已存在
    exists = any(
        (r.user_id == uid and r.friend_id == to_uid) or
        (r.user_id == to_uid and r.friend_id == uid)
        for r in RELATIONS.values()
    )
    if not exists:
        r = FriendRelation(next_relation_id, uid, to_uid, FriendStatus.PENDING, datetime.now())
        RELATIONS[r.id] = r
        store_module = __import__("store")
        store_module.next_relation_id = max(store_module.next_relation_id, r.id + 1)
        flash("好友申请已发送", "success")
    return redirect(url_for("index"))


@app.post("/friend/accept/<int:rid>")
def accept_friend(rid):
    from store import RELATIONS
    r = RELATIONS.get(rid)
    if r and r.friend_id == current_user_id():
        r.status = FriendStatus.ACCEPTED
        flash("已通过好友申请", "success")
    redir = request.form.get("redirect", "/")
    return redirect(redir)


@app.post("/friend/reject/<int:rid>")
def reject_friend(rid):
    from store import RELATIONS
    from models import FriendStatus
    r = RELATIONS.get(rid)
    if r and r.friend_id == current_user_id():
        r.status = FriendStatus.REJECTED
        flash("已拒绝好友申请", "success")
    redir = request.form.get("redirect", "/")
    return redirect(redir)


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
