package com.socialcodia.famblah.model;

public class ModelUser  {
    public String name, mobile, status, image, uid,typing_status,bio;

    public ModelUser() {
    }

    public ModelUser(String name, String mobile, String status, String image, String uid, String typing_status, String bio) {
        this.name = name;
        this.mobile = mobile;
        this.status = status;
        this.image = image;
        this.uid = uid;
        this.typing_status = typing_status;
        this.bio = bio;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTyping_status() {
        return typing_status;
    }

    public void setTyping_status(String typing_status) {
        this.typing_status = typing_status;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
