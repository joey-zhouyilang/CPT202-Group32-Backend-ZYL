package com.group32.cpt202.zyl_project.zyl_display;/**


/**
 * CommunityPostCreateRequest 类，处理当前模块的业务逻辑或数据结构。
 */
public class CommunityPostCreateRequest {

    private Long userId;
    private String title;
    private String description;
    private String tags;
    private String imageUrl;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}