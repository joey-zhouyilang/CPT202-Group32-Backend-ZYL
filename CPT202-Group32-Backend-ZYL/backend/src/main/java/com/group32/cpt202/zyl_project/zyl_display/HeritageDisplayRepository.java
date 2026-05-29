package com.group32.cpt202.zyl_project.zyl_display;

import com.group32.cpt202.LY_heritage.entity.HeritageItem;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;/**


/**
 * HeritageDisplayRepository 类，处理当前模块的业务逻辑或数据结构。
 */
@Repository("zylDisplayRepository")
public class HeritageDisplayRepository {
    private final com.group32.cpt202.LY_heritage.repository.HeritageItemRepository delegate;

    public HeritageDisplayRepository(com.group32.cpt202.LY_heritage.repository.HeritageItemRepository delegate) {
        this.delegate = delegate;
    }

    public List<HeritageDisplay> findAllApproved() {
        return delegate.findByStatusIgnoreCaseAndCommunityPostTrueOrderByUpdatedAtDesc("APPROVED").stream()
                .map(this::toDisplay)
                .collect(Collectors.toList());
    }

    public HeritageDisplay findApprovedById(Long id) {
        return delegate.findById(id)
                .filter(item -> "APPROVED".equalsIgnoreCase(item.getStatus()))
                .filter(item -> Boolean.TRUE.equals(item.getCommunityPost()))
                .map(this::toDisplay)
                .orElse(null);
    }

    public List<HeritageDisplay> findApprovedByCategory(String category) {
        return delegate.findByCategoryIgnoreCaseAndStatusIgnoreCaseAndCommunityPostTrueOrderByUpdatedAtDesc(category, "APPROVED").stream()
                .map(this::toDisplay)
                .collect(Collectors.toList());
    }

    public List<HeritageDisplay> findRecentApproved(int limit) {
        return delegate.findTop10ByStatusIgnoreCaseOrderByUpdatedAtDesc("APPROVED").stream()
                .limit(limit)
                .map(this::toDisplay)
                .collect(Collectors.toList());
    }

    public List<HeritageDisplay> findAllPlatformApproved() {
        return delegate.findByStatusIgnoreCaseAndPlatformPublishedTrueOrderByUpdatedAtDesc("APPROVED").stream()
                .map(this::toDisplay)
                .collect(Collectors.toList());
    }

    public HeritageDisplay findPlatformApprovedById(Long id) {
        return delegate.findById(id)
                .filter(item -> "APPROVED".equalsIgnoreCase(item.getStatus()))
                .filter(item -> Boolean.TRUE.equals(item.getPlatformPublished()))
                .map(this::toDisplay)
                .orElse(null);
    }

    public List<HeritageDisplay> findPlatformApprovedByCategory(String category) {
        return delegate.findByCategoryIgnoreCaseAndStatusIgnoreCaseAndPlatformPublishedTrueOrderByUpdatedAtDesc(category, "APPROVED").stream()
                .map(this::toDisplay)
                .collect(Collectors.toList());
    }

    private HeritageDisplay toDisplay(HeritageItem item) {
        HeritageDisplay display = new HeritageDisplay();
        display.setId(item.getId());
        display.setTitle(item.getTitle());
        display.setDescription(item.getDescription());
        display.setCategory(item.getCategory());
        display.setLocation(item.getLocation());
        display.setTags(item.getTags());
        display.setExternalLink(item.getExternalLink());
        display.setImageUrl(item.getImageUrl());
        display.setContributorId(item.getContributorId());
        display.setViewCount(item.getViewCount());
        display.setPlatformPublished(item.getPlatformPublished());
        display.setMediaSummaryOnly(false);
        display.setPlatformPublishedAt(item.getPlatformPublishedAt());
        display.setCreatedAt(item.getCreatedAt());
        display.setUpdatedAt(item.getUpdatedAt());
        return display;
    }
}