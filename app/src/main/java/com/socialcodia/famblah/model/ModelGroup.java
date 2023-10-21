package com.socialcodia.famblah.model;

public class ModelGroup {

    public String creator, description, group_id, image, name, timestamp;

    public ModelGroup() {
    }

    public ModelGroup(String creator, String description, String group_id, String image, String name, String timestamp) {
        this.creator = creator;
        this.description = description;
        this.group_id = group_id;
        this.image = image;
        this.name = name;
        this.timestamp = timestamp;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
