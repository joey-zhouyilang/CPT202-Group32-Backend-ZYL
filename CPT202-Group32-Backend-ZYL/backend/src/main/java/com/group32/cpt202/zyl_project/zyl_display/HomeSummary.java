package com.group32.cpt202.zyl_project.zyl_display;

import java.util.List;/**


/**
 * HomeSummary 类，处理当前模块的业务逻辑或数据结构。
 */
public class HomeSummary {
    private long approvedHeritageCount;
    private long contributorCount;
    private long pendingApplicationCount;
    private long commentCount;
    private List<HeritageDisplay> latestHeritages;

    public long getApprovedHeritageCount() {
        return approvedHeritageCount;
    }

    public void setApprovedHeritageCount(long approvedHeritageCount) {
        this.approvedHeritageCount = approvedHeritageCount;
    }

    public long getContributorCount() {
        return contributorCount;
    }

    public void setContributorCount(long contributorCount) {
        this.contributorCount = contributorCount;
    }

    public long getPendingApplicationCount() {
        return pendingApplicationCount;
    }

    public void setPendingApplicationCount(long pendingApplicationCount) {
        this.pendingApplicationCount = pendingApplicationCount;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(long commentCount) {
        this.commentCount = commentCount;
    }

    public List<HeritageDisplay> getLatestHeritages() {
        return latestHeritages;
    }

    public void setLatestHeritages(List<HeritageDisplay> latestHeritages) {
        this.latestHeritages = latestHeritages;
    }
}