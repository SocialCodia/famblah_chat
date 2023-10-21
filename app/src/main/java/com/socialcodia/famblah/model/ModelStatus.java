package com.socialcodia.famblah.model;

public class ModelStatus {
    public String image, sender_id, status_content, timestamp,status_id,name;


    public ModelStatus() {
    }

    public ModelStatus(String image, String sender_id, String status_content, String timestamp, String status_id, String name) {
        this.image = image;
        this.sender_id = sender_id;
        this.status_content = status_content;
        this.timestamp = timestamp;
        this.status_id = status_id;
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getSender_id() {
        return sender_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public String getStatus_content() {
        return status_content;
    }

    public void setStatus_content(String status_content) {
        this.status_content = status_content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus_id() {
        return status_id;
    }

    public void setStatus_id(String status_id) {
        this.status_id = status_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
