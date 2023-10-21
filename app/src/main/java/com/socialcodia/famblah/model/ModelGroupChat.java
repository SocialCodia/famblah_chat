package com.socialcodia.famblah.model;

public class ModelGroupChat {
    public String sender, message, mid, timestamp, type, image, group_id;
    public int status;

    public ModelGroupChat() {
    }

    public ModelGroupChat(String sender, String message, String mid, String timestamp, String type, String image, String group_id, int status) {
        this.sender = sender;
        this.message = message;
        this.mid = mid;
        this.timestamp = timestamp;
        this.type = type;
        this.image = image;
        this.group_id = group_id;
        this.status = status;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
