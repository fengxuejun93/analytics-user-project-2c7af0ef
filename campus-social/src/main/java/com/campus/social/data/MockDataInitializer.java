package com.campus.social.data;

import com.campus.social.model.*;
import com.campus.social.model.FriendRelation.FriendStatus;
import com.campus.social.model.SystemAlert.AlertStatus;
import com.campus.social.model.SystemAlert.AlertType;
import com.campus.social.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class MockDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final FriendRelationRepository friendRelationRepository;
    private final PhotoPostRepository photoPostRepository;
    private final CommentRepository commentRepository;
    private final SystemAlertRepository systemAlertRepository;
    private final AuditLogRepository auditLogRepository;

    public MockDataInitializer(UserRepository userRepository,
                               FriendRelationRepository friendRelationRepository,
                               PhotoPostRepository photoPostRepository,
                               CommentRepository commentRepository,
                               SystemAlertRepository systemAlertRepository,
                               AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.friendRelationRepository = friendRelationRepository;
        this.photoPostRepository = photoPostRepository;
        this.commentRepository = commentRepository;
        this.systemAlertRepository = systemAlertRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void run(String... args) {
        initUsers();
        initFriendRelations();
        initPosts();
        initComments();
        initAlerts();
        initAuditLogs();
    }

    private void initUsers() {
        userRepository.save(new User(1L, "liming", "李明", "/images/avatar1.svg",
                "北京大学", "计算机2022级1班", "男", LocalDate.of(2003, 5, 12), "热爱编程，喜欢打篮球"));
        userRepository.save(new User(2L, "wangfang", "王芳", "/images/avatar2.svg",
                "北京大学", "计算机2022级1班", "女", LocalDate.of(2003, 8, 23), "读书和旅行是我最大的爱好"));
        userRepository.save(new User(3L, "zhangwei", "张伟", "/images/avatar3.svg",
                "清华大学", "软件工程2022级2班", "男", LocalDate.of(2003, 3, 15), "代码改变世界"));
        userRepository.save(new User(4L, "liuna", "刘娜", "/images/avatar4.svg",
                "北京大学", "计算机2022级2班", "女", LocalDate.of(2003, 11, 8), "生活需要仪式感"));
        userRepository.save(new User(5L, "chenjie", "陈杰", "/images/avatar5.svg",
                "北京大学", "计算机2022级1班", "男", LocalDate.of(2003, 7, 20), "羽毛球爱好者"));
        userRepository.save(new User(6L, "zhaoyang", "赵阳", "/images/avatar6.svg",
                "清华大学", "软件工程2022级2班", "男", LocalDate.of(2003, 1, 30), "摄影与骑行"));
    }

    private void initFriendRelations() {
        // 李明 和 王芳 互为好友
        friendRelationRepository.save(new FriendRelation(1L, 1L, 2L, FriendStatus.ACCEPTED,
                LocalDateTime.of(2024, 3, 15, 10, 0)));
        // 李明 和 陈杰 互为好友
        friendRelationRepository.save(new FriendRelation(2L, 1L, 5L, FriendStatus.ACCEPTED,
                LocalDateTime.of(2024, 3, 20, 14, 30)));
        // 李明 和 刘娜 互为好友
        friendRelationRepository.save(new FriendRelation(3L, 4L, 1L, FriendStatus.ACCEPTED,
                LocalDateTime.of(2024, 4, 5, 9, 15)));
        // 王芳 和 刘娜 互为好友
        friendRelationRepository.save(new FriendRelation(4L, 2L, 4L, FriendStatus.ACCEPTED,
                LocalDateTime.of(2024, 4, 10, 16, 0)));
        // 王芳 和 陈杰 互为好友
        friendRelationRepository.save(new FriendRelation(5L, 5L, 2L, FriendStatus.ACCEPTED,
                LocalDateTime.of(2024, 5, 1, 11, 30)));
        // 张伟 向 李明 发了好友申请（待处理）
        friendRelationRepository.save(new FriendRelation(6L, 3L, 1L, FriendStatus.PENDING,
                LocalDateTime.of(2024, 6, 10, 8, 45)));
        // 刘娜 向 陈杰 发了好友申请（待处理）
        friendRelationRepository.save(new FriendRelation(7L, 4L, 5L, FriendStatus.PENDING,
                LocalDateTime.of(2024, 6, 12, 13, 20)));
        // 张伟 和 赵阳 互为好友
        friendRelationRepository.save(new FriendRelation(8L, 3L, 6L, FriendStatus.ACCEPTED,
                LocalDateTime.of(2024, 2, 14, 10, 0)));
        // 赵阳 曾向 李明 发过好友申请被拒绝（测试反向申请翻转）
        friendRelationRepository.save(new FriendRelation(9L, 6L, 1L, FriendStatus.REJECTED,
                LocalDateTime.of(2024, 6, 20, 19, 30)));
    }

    private void initPosts() {
        // 王芳的公开动态
        photoPostRepository.save(new PhotoPost(1L, 2L, "今天校园的樱花开了，好美！🌸",
                "/images/photo1.svg", Visibility.PUBLIC, 5, LocalDateTime.of(2024, 6, 15, 9, 30)));
        // 李明的仅好友动态
        photoPostRepository.save(new PhotoPost(2L, 1L, "和同学们一起做项目，收获满满！",
                "/images/photo2.svg", Visibility.FRIENDS_ONLY, 3, LocalDateTime.of(2024, 6, 14, 15, 20)));
        // 张伟的公开动态
        photoPostRepository.save(new PhotoPost(3L, 3L, "清华园的秋天，每一步都是风景",
                "/images/photo3.svg", Visibility.PUBLIC, 8, LocalDateTime.of(2024, 6, 13, 10, 45)));
        // 刘娜的仅好友动态
        photoPostRepository.save(new PhotoPost(4L, 4L, "周末烘焙课的成果，满满的成就感！",
                "/images/photo4.svg", Visibility.FRIENDS_ONLY, 6, LocalDateTime.of(2024, 6, 12, 18, 0)));
        // 李明的仅自己动态
        photoPostRepository.save(new PhotoPost(5L, 1L, "今天心情不太好，写点日记...",
                "/images/photo5.svg", Visibility.PRIVATE, 0, LocalDateTime.of(2024, 6, 11, 22, 30)));
        // 陈杰的公开动态
        photoPostRepository.save(new PhotoPost(6L, 5L, "羽毛球比赛拿了第二名！继续加油！",
                "/images/photo6.svg", Visibility.PUBLIC, 12, LocalDateTime.of(2024, 6, 10, 16, 45)));
        // 赵阳的公开动态
        photoPostRepository.save(new PhotoPost(7L, 6L, "骑行到长城，风光无限好",
                "/images/photo7.svg", Visibility.PUBLIC, 4, LocalDateTime.of(2024, 6, 9, 8, 0)));
        // 王芳的仅好友动态
        photoPostRepository.save(new PhotoPost(8L, 2L, "和室友的下午茶时光~",
                "/images/photo8.svg", Visibility.FRIENDS_ONLY, 7, LocalDateTime.of(2024, 6, 8, 14, 30)));
    }

    private void initComments() {
        // 动态1的评论
        Comment c1 = new Comment(1L, 1L, 1L, "好漂亮！下次带我一起去看",
                LocalDateTime.of(2024, 6, 15, 10, 0));
        commentRepository.saveComment(c1);
        commentRepository.saveReply(new Reply(1L, 1L, 2L, "好呀，约起！",
                LocalDateTime.of(2024, 6, 15, 10, 15)));

        Comment c2 = new Comment(2L, 1L, 5L, "我也想去！",
                LocalDateTime.of(2024, 6, 15, 11, 30));
        commentRepository.saveComment(c2);

        // 动态2的评论
        Comment c3 = new Comment(3L, 2L, 2L, "李明同学棒棒的！",
                LocalDateTime.of(2024, 6, 14, 16, 0));
        commentRepository.saveComment(c3);
        commentRepository.saveReply(new Reply(2L, 3L, 1L, "谢谢王芳鼓励！",
                LocalDateTime.of(2024, 6, 14, 16, 20)));

        // 动态6的评论
        Comment c4 = new Comment(4L, 6L, 1L, "陈杰太厉害了！下次教教我",
                LocalDateTime.of(2024, 6, 10, 17, 0));
        commentRepository.saveComment(c4);
        commentRepository.saveReply(new Reply(3L, 4L, 5L, "没问题，随时约球！",
                LocalDateTime.of(2024, 6, 10, 17, 30)));

        Comment c5 = new Comment(5L, 6L, 2L, "运动健将！",
                LocalDateTime.of(2024, 6, 10, 18, 0));
        commentRepository.saveComment(c5);
    }

    private void initAlerts() {
        // 张伟向李明的好友申请 → 告警给李明
        systemAlertRepository.save(new SystemAlert(1L, AlertType.FRIEND_REQUEST,
                "张伟向你发送了好友申请", 3L, 1L, 6L, AlertStatus.PENDING,
                LocalDateTime.of(2024, 6, 10, 8, 45)));
        // 刘娜向陈杰的好友申请 → 告警给陈杰
        systemAlertRepository.save(new SystemAlert(2L, AlertType.FRIEND_REQUEST,
                "刘娜向你发送了好友申请", 4L, 5L, 7L, AlertStatus.PENDING,
                LocalDateTime.of(2024, 6, 12, 13, 20)));
        // 李明评论王芳动态 → 告警给王芳（已派发给王芳自己处理）
        SystemAlert a3 = new SystemAlert(3L, AlertType.POST_COMMENT,
                "李明评论了你的动态", 1L, 2L, 1L, AlertStatus.DISPATCHED,
                LocalDateTime.of(2024, 6, 15, 10, 0));
        a3.setAssigneeId(2L);
        a3.setDispatchedAt(LocalDateTime.of(2024, 6, 15, 10, 5));
        systemAlertRepository.save(a3);
        // 陈杰评论王芳动态 → 告警给王芳（已关闭）
        SystemAlert a4 = new SystemAlert(4L, AlertType.POST_COMMENT,
                "陈杰评论了你的动态", 5L, 2L, 1L, AlertStatus.RESOLVED,
                LocalDateTime.of(2024, 6, 15, 11, 30));
        a4.setAssigneeId(2L);
        a4.setDispatchedAt(LocalDateTime.of(2024, 6, 15, 11, 35));
        a4.setResolveNote("已知悉");
        a4.setResolvedAt(LocalDateTime.of(2024, 6, 15, 12, 0));
        systemAlertRepository.save(a4);
        // 李明评论陈杰动态 → 告警给陈杰
        systemAlertRepository.save(new SystemAlert(5L, AlertType.POST_COMMENT,
                "李明评论了你的动态", 1L, 5L, 6L, AlertStatus.PENDING,
                LocalDateTime.of(2024, 6, 10, 17, 0)));
        // 王芳回复李明评论 → 告警给李明（已派发）
        SystemAlert a6 = new SystemAlert(6L, AlertType.COMMENT_REPLY,
                "王芳回复了你的评论", 2L, 1L, 1L, AlertStatus.DISPATCHED,
                LocalDateTime.of(2024, 6, 15, 10, 15));
        a6.setAssigneeId(1L);
        a6.setDispatchedAt(LocalDateTime.of(2024, 6, 15, 10, 20));
        systemAlertRepository.save(a6);
    }

    private void initAuditLogs() {
        auditLogRepository.save(new AuditLog(1L, 2L, "CREATE_POST", "POST", 1L,
                "发布动态: 今天校园的樱花开了", LocalDateTime.of(2024, 6, 15, 9, 30)));
        auditLogRepository.save(new AuditLog(2L, 1L, "CREATE_POST", "POST", 2L,
                "发布动态: 和同学们一起做项目", LocalDateTime.of(2024, 6, 14, 15, 20)));
        auditLogRepository.save(new AuditLog(3L, 3L, "SEND_REQUEST", "FRIEND_RELATION", 6L,
                "发送好友申请", LocalDateTime.of(2024, 6, 10, 8, 45)));
        auditLogRepository.save(new AuditLog(4L, 4L, "SEND_REQUEST", "FRIEND_RELATION", 7L,
                "发送好友申请", LocalDateTime.of(2024, 6, 12, 13, 20)));
        auditLogRepository.save(new AuditLog(5L, 1L, "COMMENT", "POST", 1L,
                "评论动态#1: 好漂亮！", LocalDateTime.of(2024, 6, 15, 10, 0)));
        auditLogRepository.save(new AuditLog(6L, 2L, "REPLY", "COMMENT", 1L,
                "回复评论#1: 好呀", LocalDateTime.of(2024, 6, 15, 10, 15)));
        auditLogRepository.save(new AuditLog(7L, 5L, "COMMENT", "POST", 1L,
                "评论动态#1: 我也想去！", LocalDateTime.of(2024, 6, 15, 11, 30)));
        auditLogRepository.save(new AuditLog(8L, 2L, "DISPATCH_ALERT", "ALERT", 3L,
                "派发告警 #3", LocalDateTime.of(2024, 6, 15, 10, 5)));
        auditLogRepository.save(new AuditLog(9L, 2L, "RESOLVE_ALERT", "ALERT", 4L,
                "关闭告警 #4：已知悉", LocalDateTime.of(2024, 6, 15, 12, 0)));
        auditLogRepository.save(new AuditLog(10L, 1L, "LIKE_POST", "POST", 1L,
                "LIKE_POST", LocalDateTime.of(2024, 6, 15, 12, 30)));
    }
}
