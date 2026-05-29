package com.group32.cpt202.zyl_project.zyl_display;

import java.time.LocalDateTime;/**


/**
 * HeritageDisplay 类，处理当前模块的业务逻辑或数据结构。
 */
public class HeritageDisplay {
    private Long id;
    private String title;
    private String description;
    private String category;
    private String location;
    private String tags;
    private String externalLink;
    private String imageUrl;
    private Long contributorId;
    private String contributorName;
    private String contributorAvatarUrl;
    private Long commentCount;
    private Long likeCount;
    private Long shareCount;
    private Boolean likedByCurrentUser;
    private Integer viewCount;
    private Boolean platformPublished;
    private Boolean mediaSummaryOnly;
    private LocalDateTime platformPublishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getExternalLink() {
        return externalLink;
    }

    public void setExternalLink(String externalLink) {
        this.externalLink = externalLink;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getContributorId() {
        return contributorId;
    }

    public void setContributorId(Long contributorId) {
        this.contributorId = contributorId;
    }

    public String getContributorName() {
        return contributorName;
    }

    public void setContributorName(String contributorName) {
        this.contributorName = contributorName;
    }

    public String getContributorAvatarUrl() {
        return contributorAvatarUrl;
    }

    public void setContributorAvatarUrl(String contributorAvatarUrl) {
        this.contributorAvatarUrl = contributorAvatarUrl;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    public Long getShareCount() {
        return shareCount;
    }

    public void setShareCount(Long shareCount) {
        this.shareCount = shareCount;
    }

    public Boolean getLikedByCurrentUser() {
        return likedByCurrentUser;
    }

    public void setLikedByCurrentUser(Boolean likedByCurrentUser) {
        this.likedByCurrentUser = likedByCurrentUser;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Boolean getPlatformPublished() {
        return platformPublished;
    }

    public void setPlatformPublished(Boolean platformPublished) {
        this.platformPublished = platformPublished;
    }

    public Boolean getMediaSummaryOnly() {
        return mediaSummaryOnly;
    }

    public void setMediaSummaryOnly(Boolean mediaSummaryOnly) {
        this.mediaSummaryOnly = mediaSummaryOnly;
    }

    public LocalDateTime getPlatformPublishedAt() {
        return platformPublishedAt;
    }

    public void setPlatformPublishedAt(LocalDateTime platformPublishedAt) {
        this.platformPublishedAt = platformPublishedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}