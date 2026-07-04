package com.campus.social.model;

import java.time.LocalDate;

public class User {
    private Long id;
    private String username;
    private String realName;
    private String avatar;
    private String school;
    private String className;
    private String gender;
    private LocalDate birthday;
    private String bio;

    public User() {}

    public User(Long id, String username, String realName, String avatar,
                String school, String className, String gender,
                LocalDate birthday, String bio) {
        this.id = id;
        this.username = username;
        this.realName = realName;
        this.avatar = avatar;
        this.school = school;
        this.className = className;
        this.gender = gender;
        this.birthday = birthday;
        this.bio = bio;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public LocalDate getBirthday() { return birthday; }
    public void setBirthday(LocalDate birthday) { this.birthday = birthday; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}
