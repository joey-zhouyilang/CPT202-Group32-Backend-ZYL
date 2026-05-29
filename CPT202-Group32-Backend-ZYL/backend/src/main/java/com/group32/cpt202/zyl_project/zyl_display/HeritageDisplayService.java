package com.group32.cpt202.zyl_project.zyl_display;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group32.cpt202.LY_contributor.entity.ContributorApplication;
import com.group32.cpt202.LY_contributor.entity.User;
import com.group32.cpt202.LY_contributor.repository.ContributorApplicationRepository;
import com.group32.cpt202.LY_contributor.repository.UserRepository;
import com.group32.cpt202.LY_heritage.dto.HeritageCommentDTO;
import com.group32.cpt202.LY_heritage.entity.ForumPostLike;
import com.group32.cpt202.LY_heritage.entity.Message;
import com.group32.cpt202.LY_heritage.repository.ForumPostLikeRepository;
import com.group32.cpt202.LY_heritage.repository.HeritageItemRepository;
import com.group32.cpt202.LY_heritage.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;


// 服务层，封装业务逻辑。
/**
 * HeritageDisplayService 类，处理当前模块的业务逻辑或数据结构。
 */
@Service
public class HeritageDisplayService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int LIST_IMAGE_MAX_WIDTH = 480;
    private static final int LIST_IMAGE_MAX_HEIGHT = 320;

    private final HeritageDisplayRepository repository;
    private final UserRepository userRepository;
    private final ContributorApplicationRepository contributorApplicationRepository;
    private final HeritageItemRepository heritageItemRepository;
    private final MessageRepository messageRepository;
    private final ForumPostLikeRepository forumPostLikeRepository;

    public HeritageDisplayService(HeritageDisplayRepository repository,
                                  UserRepository userRepository,
                                  ContributorApplicationRepository contributorApplicationRepository,
                                  HeritageItemRepository heritageItemRepository,
                                  MessageRepository messageRepository,
                                  ForumPostLikeRepository forumPostLikeRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.contributorApplicationRepository = contributorApplicationRepository;
        this.heritageItemRepository = heritageItemRepository;
        this.messageRepository = messageRepository;
        this.forumPostLikeRepository = forumPostLikeRepository;
    }

    public List<HeritageDisplay> getAll(Long currentUserId) {
        List<HeritageDisplay> displays = messageRepository
                .findByForumPostIdIsNullAndHeritageIdIsNullAndTitleIsNotNullOrderBySentAtDesc()
                .stream()
                .map(this::toCommunityDisplay)
                .toList();
        return summarizeCommunityDisplays(enrichDisplays(displays, currentUserId));
    }

    public HeritageDisplay getById(Long id, Long currentUserId) {
        return enrichDisplay(toCommunityDisplay(getForumPostOrThrow(id)), currentUserId);
    }

    public List<HeritageDisplay> getByCategory(String category, Long currentUserId) {
        String normalizedCategory = category == null ? "" : category.trim().toLowerCase();
        List<HeritageDisplay> displays = messageRepository
                .findByForumPostIdIsNullAndHeritageIdIsNullAndTitleIsNotNullOrderBySentAtDesc()
                .stream()
                .map(this::toCommunityDisplay)
                .filter(display -> normalizedCategory.equals(String.valueOf(display.getCategory()).toLowerCase()))
                .toList();
        return summarizeCommunityDisplays(enrichDisplays(displays, currentUserId));
    }

    public List<HeritageDisplay> getPlatformApproved() {
        return summarizePlatformDisplays(enrichDisplays(repository.findAllPlatformApproved(), null));
    }

    public HeritageDisplay getPlatformById(Long id) {
        HeritageDisplay display = repository.findPlatformApprovedById(id);
        if (display == null) {
            throw new RuntimeException("platform heritage not found");
        }
        return enrichDisplay(display, null);
    }

    public List<HeritageDisplay> getPlatformApprovedByCategory(String category) {
        return summarizePlatformDisplays(enrichDisplays(repository.findPlatformApprovedByCategory(category), null));
    }

    public HomeSummary getHomeSummary() {
        HomeSummary summary = new HomeSummary();
        summary.setApprovedHeritageCount(heritageItemRepository.countByStatusIgnoreCase("APPROVED"));
        summary.setContributorCount(userRepository.countByRole(User.Role.CONTRIBUTOR));
        summary.setPendingApplicationCount(
                contributorApplicationRepository.countByStatus(ContributorApplication.Status.PENDING)
        );
        summary.setCommentCount(messageRepository.countByHeritageIdIsNotNull());
        summary.setLatestHeritages(enrichDisplays(repository.findRecentApproved(6), null));
        return summary;
    }

// 事务边界，确保数据库操作要么全部成功，要么全部回滚。
    @Transactional
    public HeritageDisplay createCommunityPost(CommunityPostCreateRequest request) {
        if (request == null) {
            throw new RuntimeException("request is required");
        }
        if (request.getUserId() == null) {
            throw new RuntimeException("userId is required");
        }
        String normalizedTitle = trimToNull(request.getTitle());
        if (normalizedTitle == null) {
            throw new RuntimeException("title is required");
        }
        String normalizedDescription = trimToNull(request.getDescription());
        if (normalizedDescription == null) {
            throw new RuntimeException("description is required");
        }
        String normalizedMedia = trimToNull(request.getImageUrl());
        if (normalizedMedia == null) {
            throw new RuntimeException("media is required");
        }
        validateCommunityPostMedia(normalizedMedia);

        User user = userRepository.findById(request.getUserId()).orElse(null);
        if (user == null) {
            throw new RuntimeException("user not found");
        }

        Message post = new Message();
        post.setUserId(user.getId());
        post.setTitle(normalizedTitle);
        post.setContent(normalizedDescription);
        post.setTags(normalizeTags(request.getTags()));
        post.setImageUrl(normalizedMedia);
        post.setLikeCount(0L);
        post.setShareCount(0L);
        post.setSentAt(LocalDateTime.now());

        Message savedPost = messageRepository.save(post);
        return enrichDisplay(toCommunityDisplay(savedPost), user.getId());
    }

// 事务边界，确保数据库操作要么全部成功，要么全部回滚。
    @Transactional
    public void deleteCommunityPost(Long postId, Long userId) {
        if (postId == null) {
            throw new RuntimeException("postId is required");
        }
        if (userId == null) {
            throw new RuntimeException("userId is required");
        }

        Message post = getForumPostOrThrow(postId);
        if (!Objects.equals(post.getUserId(), userId)) {
            throw new RuntimeException("only the post author can delete this post");
        }

        forumPostLikeRepository.deleteByPostId(postId);
        messageRepository.deleteByForumPostId(postId);
        messageRepository.delete(post);
    }

// 事务边界，确保数据库操作要么全部成功，要么全部回滚。
    @Transactional
    public HeritageDisplay toggleCommunityPostLike(Long postId, Long userId) {
        if (postId == null) {
            throw new RuntimeException("postId is required");
        }
        if (userId == null) {
            throw new RuntimeException("userId is required");
        }

        Message post = getForumPostOrThrow(postId);
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new RuntimeException("user not found");
        }

        ForumPostLike existingLike = forumPostLikeRepository.findByPostIdAndUserId(postId, userId).orElse(null);
        if (existingLike != null) {
            forumPostLikeRepository.delete(existingLike);
            post.setLikeCount(Math.max(0L, safeCount(post.getLikeCount()) - 1L));
        } else {
            ForumPostLike like = new ForumPostLike();
            like.setPostId(postId);
            like.setUserId(userId);
            forumPostLikeRepository.save(like);
            post.setLikeCount(safeCount(post.getLikeCount()) + 1L);
        }

        Message savedPost = messageRepository.save(post);
        return enrichDisplay(toCommunityDisplay(savedPost), userId);
    }

// 事务边界，确保数据库操作要么全部成功，要么全部回滚。
    @Transactional
    public HeritageDisplay incrementCommunityPostShare(Long postId, Long currentUserId) {
        if (postId == null) {
            throw new RuntimeException("postId is required");
        }

        Message post = getForumPostOrThrow(postId);
        post.setShareCount(safeCount(post.getShareCount()) + 1L);
        Message savedPost = messageRepository.save(post);
        return enrichDisplay(toCommunityDisplay(savedPost), currentUserId);
    }

    public List<HeritageCommentDTO> getCommunityComments(Long postId) {
        getForumPostOrThrow(postId);
        return buildCommunityCommentTree(postId);
    }

// 事务边界，确保数据库操作要么全部成功，要么全部回滚。
    @Transactional
    public List<HeritageCommentDTO> addCommunityComment(Long postId, Long userId, String content, Long parentMessageId) {
        Message post = getForumPostOrThrow(postId);
        if (userId == null) {
            throw new RuntimeException("userId is required");
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new RuntimeException("user not found");
        }

        String normalizedContent = trimToNull(content);
        if (normalizedContent == null) {
            throw new RuntimeException("content is required");
        }

        if (parentMessageId != null) {
            Message parent = messageRepository.findById(parentMessageId).orElse(null);
            if (parent == null || !Objects.equals(parent.getForumPostId(), post.getId())) {
                throw new RuntimeException("parent comment not found");
            }
        }

        Message comment = new Message();
        comment.setUserId(userId);
        comment.setForumPostId(post.getId());
        comment.setParentMessageId(parentMessageId);
        comment.setContent(normalizedContent);
        comment.setSentAt(LocalDateTime.now());

        messageRepository.save(comment);
        return buildCommunityCommentTree(postId);
    }

    private Message getForumPostOrThrow(Long postId) {
        return messageRepository.findById(postId)
                .filter(message -> message.getForumPostId() == null)
                .filter(message -> message.getHeritageId() == null)
                .filter(message -> message.getTitle() != null && !message.getTitle().isBlank())
                .orElseThrow(() -> new RuntimeException("community post not found"));
    }

    private List<HeritageDisplay> enrichDisplays(List<HeritageDisplay> displays, Long currentUserId) {
        if (displays == null || displays.isEmpty()) {
            return displays;
        }

        Map<Long, User> contributorsById = loadContributors(displays);
        Map<Long, Long> commentCountsByPostId = loadCommentCounts(displays);
        Set<Long> likedPostIds = loadLikedPostIds(displays, currentUserId);

        for (HeritageDisplay display : displays) {
            applyDisplayEnrichment(display, contributorsById, commentCountsByPostId, likedPostIds, currentUserId);
        }
        return displays;
    }

    private HeritageDisplay enrichDisplay(HeritageDisplay display, Long currentUserId) {
        if (display == null) {
            return null;
        }

        List<HeritageDisplay> singleDisplay = new ArrayList<>();
        singleDisplay.add(display);
        enrichDisplays(singleDisplay, currentUserId);
        return display;
    }

    private void applyDisplayEnrichment(HeritageDisplay display,
                                        Map<Long, User> contributorsById,
                                        Map<Long, Long> commentCountsByPostId,
                                        Set<Long> likedPostIds,
                                        Long currentUserId) {
        if (display == null) {
            return;
        }

        if (display.getContributorId() != null) {
            User contributor = contributorsById.get(display.getContributorId());
            if (contributor != null) {
                display.setContributorName(contributor.getUsername());
                display.setContributorAvatarUrl(contributor.getAvatarUrl());
            }
        }

        if (display.getId() != null) {
            display.setCommentCount(commentCountsByPostId.getOrDefault(display.getId(), 0L));
            display.setLikedByCurrentUser(currentUserId != null && likedPostIds.contains(display.getId()));
        }
    }

    private Map<Long, User> loadContributors(List<HeritageDisplay> displays) {
        List<Long> contributorIds = displays.stream()
                .map(HeritageDisplay::getContributorId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        return loadUsersById(contributorIds);
    }

    private Map<Long, User> loadUsersById(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private Map<Long, Long> loadCommentCounts(List<HeritageDisplay> displays) {
        List<Long> postIds = displays.stream()
                .map(HeritageDisplay::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (postIds.isEmpty()) {
            return Map.of();
        }

        return messageRepository.findByForumPostIdIn(postIds).stream()
                .filter(message -> message.getForumPostId() != null)
                .collect(Collectors.groupingBy(Message::getForumPostId, Collectors.counting()));
    }

    private Set<Long> loadLikedPostIds(List<HeritageDisplay> displays, Long currentUserId) {
        if (currentUserId == null) {
            return Set.of();
        }

        List<Long> postIds = displays.stream()
                .map(HeritageDisplay::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (postIds.isEmpty()) {
            return Set.of();
        }

        return forumPostLikeRepository.findByUserIdAndPostIdIn(currentUserId, postIds).stream()
                .map(ForumPostLike::getPostId)
                .collect(Collectors.toSet());
    }

    private HeritageDisplay toCommunityDisplay(Message post) {
        HeritageDisplay display = new HeritageDisplay();
        display.setId(post.getId());
        display.setTitle(post.getTitle());
        display.setDescription(post.getContent());
        display.setCategory(deriveCategory(post.getTags(), post.getTitle(), post.getContent()));
        display.setLocation("Community");
        display.setTags(post.getTags());
        display.setExternalLink(null);
        display.setImageUrl(post.getImageUrl());
        display.setContributorId(post.getUserId());
        display.setViewCount(0);
        display.setLikeCount(safeCount(post.getLikeCount()));
        display.setShareCount(safeCount(post.getShareCount()));
        display.setLikedByCurrentUser(false);
        display.setPlatformPublished(false);
        display.setMediaSummaryOnly(false);
        display.setPlatformPublishedAt(null);
        display.setCreatedAt(post.getSentAt());
        display.setUpdatedAt(post.getSentAt());
        return display;
    }

    private List<HeritageDisplay> summarizeCommunityDisplays(List<HeritageDisplay> displays) {
        for (HeritageDisplay display : displays) {
            MediaSummaryResult result = summarizeCommunityMedia(display.getImageUrl());
            display.setImageUrl(result.mediaValue());
            display.setMediaSummaryOnly(result.summaryOnly());
        }
        return displays;
    }

    private List<HeritageDisplay> summarizePlatformDisplays(List<HeritageDisplay> displays) {
        for (HeritageDisplay display : displays) {
            MediaSummaryResult result = summarizePlatformMedia(display.getImageUrl());
            display.setImageUrl(result.mediaValue());
            display.setMediaSummaryOnly(result.summaryOnly());
        }
        return displays;
    }

    private MediaSummaryResult summarizeCommunityMedia(String mediaValue) {
        List<String> mediaEntries = parseMediaEntries(mediaValue);
        if (mediaEntries.isEmpty()) {
            return new MediaSummaryResult(trimToNull(mediaValue), false);
        }

        List<String> summarizedEntries = new ArrayList<>();
        boolean summaryOnly = false;
        for (String mediaEntry : mediaEntries) {
            String summarizedEntry = summarizeListMediaEntry(mediaEntry);
            if (summarizedEntry == null) {
                summaryOnly = true;
                continue;
            }
            if (!Objects.equals(summarizedEntry, mediaEntry)) {
                summaryOnly = true;
            }
            summarizedEntries.add(summarizedEntry);
        }

        if (summarizedEntries.isEmpty()) {
            return new MediaSummaryResult(null, true);
        }

        return new MediaSummaryResult(serializeMediaEntries(summarizedEntries), summaryOnly);
    }

    private MediaSummaryResult summarizePlatformMedia(String mediaValue) {
        List<String> mediaEntries = parseMediaEntries(mediaValue);
        if (mediaEntries.isEmpty()) {
            return new MediaSummaryResult(null, false);
        }

        String chosenPreview = null;
        boolean summaryOnly = false;
        for (String mediaEntry : mediaEntries) {
            String summarizedEntry = summarizePlatformPreviewEntry(mediaEntry);
            if (summarizedEntry == null) {
                summaryOnly = true;
                continue;
            }

            if (chosenPreview == null) {
                chosenPreview = summarizedEntry;
            } else {
                summaryOnly = true;
            }

            if (!Objects.equals(summarizedEntry, mediaEntry)) {
                summaryOnly = true;
            }
        }

        if (chosenPreview == null) {
            return new MediaSummaryResult(null, true);
        }

        if (mediaEntries.size() > 1) {
            summaryOnly = true;
        }
        return new MediaSummaryResult(chosenPreview, summaryOnly);
    }

    private String summarizeListMediaEntry(String mediaEntry) {
        String normalizedEntry = trimToNull(mediaEntry);
        if (normalizedEntry == null) {
            return null;
        }
        if (isImageMedia(normalizedEntry)) {
            return createListImagePreview(normalizedEntry);
        }
        if (isEmbeddedMedia(normalizedEntry)) {
            return null;
        }
        return normalizedEntry;
    }

    private String summarizePlatformPreviewEntry(String mediaEntry) {
        String normalizedEntry = trimToNull(mediaEntry);
        if (normalizedEntry == null || !isImageMedia(normalizedEntry)) {
            return null;
        }
        return createListImagePreview(normalizedEntry);
    }

    private String serializeMediaEntries(List<String> mediaEntries) {
        if (mediaEntries == null || mediaEntries.isEmpty()) {
            return null;
        }
        if (mediaEntries.size() == 1) {
            return mediaEntries.get(0);
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(mediaEntries);
        } catch (Exception ignored) {
            return mediaEntries.get(0);
        }
    }

    private String createListImagePreview(String mediaEntry) {
        String normalizedEntry = trimToNull(mediaEntry);
        if (normalizedEntry == null || !normalizedEntry.regionMatches(true, 0, "data:image/", 0, 11)) {
            return normalizedEntry;
        }

        int commaIndex = normalizedEntry.indexOf(',');
        int semicolonIndex = normalizedEntry.indexOf(';');
        if (commaIndex < 0 || semicolonIndex < 0 || semicolonIndex > commaIndex) {
            return normalizedEntry;
        }

        String mimeType = normalizedEntry.substring(5, semicolonIndex).toLowerCase();
        String base64Payload = normalizedEntry.substring(commaIndex + 1);

        try {
            byte[] sourceBytes = Base64.getDecoder().decode(base64Payload);
            BufferedImage sourceImage = ImageIO.read(new ByteArrayInputStream(sourceBytes));
            if (sourceImage == null) {
                return normalizedEntry;
            }

            int sourceWidth = sourceImage.getWidth();
            int sourceHeight = sourceImage.getHeight();
            if (sourceWidth <= 0 || sourceHeight <= 0) {
                return normalizedEntry;
            }

            double scaleRatio = Math.min(
                    1.0,
                    Math.min(
                            (double) LIST_IMAGE_MAX_WIDTH / sourceWidth,
                            (double) LIST_IMAGE_MAX_HEIGHT / sourceHeight
                    )
            );
            if (scaleRatio >= 1.0d && normalizedEntry.length() <= 24_000) {
                return normalizedEntry;
            }

            int targetWidth = Math.max(1, (int) Math.round(sourceWidth * scaleRatio));
            int targetHeight = Math.max(1, (int) Math.round(sourceHeight * scaleRatio));
            boolean hasAlpha = sourceImage.getColorModel().hasAlpha();
            String outputFormat = hasAlpha ? "png" : "jpg";
            String outputMimeType = hasAlpha ? "image/png" : "image/jpeg";
            BufferedImage resizedImage = new BufferedImage(
                    targetWidth,
                    targetHeight,
                    hasAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB
            );

            Graphics2D graphics = resizedImage.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.drawImage(sourceImage, 0, 0, targetWidth, targetHeight, null);
            graphics.dispose();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            if (!ImageIO.write(resizedImage, outputFormat, outputStream)) {
                return normalizedEntry;
            }

            String summarizedEntry = "data:" + outputMimeType + ";base64," +
                    Base64.getEncoder().encodeToString(outputStream.toByteArray());
            if (summarizedEntry.length() >= normalizedEntry.length()) {
                return normalizedEntry;
            }
            return summarizedEntry;
        } catch (Exception ignored) {
            return normalizedEntry;
        }
    }

    private boolean isEmbeddedMedia(String value) {
        return String.valueOf(value).trim().toLowerCase().startsWith("data:");
    }

    private boolean isImageMedia(String value) {
        String normalized = String.valueOf(value).trim().toLowerCase();
        if (normalized.isEmpty()) {
            return false;
        }
        if (normalized.startsWith("data:image/")) {
            return true;
        }
        return normalized.matches(".*\\.(png|jpe?g|gif|bmp|webp|svg|heic|heif)(?:\\?.*)?$");
    }

    private List<HeritageCommentDTO> buildCommunityCommentTree(Long postId) {
        List<Message> messages = messageRepository.findByForumPostIdOrderBySentAtAsc(postId);
        Map<Long, HeritageCommentDTO> commentsById = new LinkedHashMap<>();
        Map<Long, User> authorsById = loadUsersById(messages.stream()
                .map(Message::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList());

        for (Message message : messages) {
            HeritageCommentDTO comment = new HeritageCommentDTO();
            comment.setId(message.getId());
            comment.setParentMessageId(message.getParentMessageId());
            comment.setContent(message.getContent());
            comment.setSentAt(message.getSentAt());
            comment.setReplies(new ArrayList<>());

            if (message.getUserId() != null) {
                User author = authorsById.get(message.getUserId());
                if (author != null) {
                    comment.setUsername(author.getUsername());
                }
            }
            if (comment.getUsername() == null || comment.getUsername().isBlank()) {
                comment.setUsername("Unknown User");
            }

            commentsById.put(message.getId(), comment);
        }

        List<HeritageCommentDTO> topLevelComments = new ArrayList<>();
        for (Message message : messages) {
            HeritageCommentDTO comment = commentsById.get(message.getId());
            if (comment == null) {
                continue;
            }

            Long parentMessageId = message.getParentMessageId();
            if (parentMessageId == null) {
                topLevelComments.add(comment);
                continue;
            }

            HeritageCommentDTO parentComment = commentsById.get(parentMessageId);
            if (parentComment == null) {
                topLevelComments.add(comment);
                continue;
            }

            comment.setReplyToUsername(parentComment.getUsername());
            if (parentComment.getReplies() == null) {
                parentComment.setReplies(new ArrayList<>());
            }
            parentComment.getReplies().add(comment);
        }

        return topLevelComments;
    }

    private String normalizeTags(String tags) {
        if (tags == null) {
            return null;
        }
        String normalized = tags.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private void validateCommunityPostMedia(String mediaValue) {
        List<String> mediaEntries = parseMediaEntries(mediaValue);
        if (mediaEntries.isEmpty()) {
            throw new RuntimeException("media is required");
        }

        boolean containsPdf = mediaEntries.stream().anyMatch(this::isPdfMedia);
        if (containsPdf) {
            throw new RuntimeException("PDF files are not supported in community posts");
        }
    }

    private List<String> parseMediaEntries(String mediaValue) {
        String normalized = trimToNull(mediaValue);
        if (normalized == null) {
            return List.of();
        }

        try {
            List<String> parsed = OBJECT_MAPPER.readValue(normalized, new TypeReference<List<String>>() {
            });
            if (parsed == null) {
                return List.of();
            }
            return parsed.stream()
                    .map(this::trimToNull)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (Exception ignored) {
            return List.of(normalized);
        }
    }

    private boolean isPdfMedia(String value) {
        String normalized = String.valueOf(value).trim().toLowerCase();
        if (normalized.isEmpty()) {
            return false;
        }
        if (normalized.startsWith("data:application/pdf")) {
            return true;
        }
        return normalized.matches(".*\\.pdf(?:\\?.*)?$");
    }

    private long safeCount(Long value) {
        return value == null ? 0L : value;
    }

    private String deriveCategory(String tags, String title, String description) {
        String source = String.join(" ",
                tags == null ? "" : tags,
                title == null ? "" : title,
                description == null ? "" : description
        ).toLowerCase();

        if (source.contains("architect") || source.contains("temple") || source.contains("building")) {
            return "architecture";
        }
        if (source.contains("relic") || source.contains("artifact")) {
            return "relics";
        }
        if (source.contains("folklore") || source.contains("folktale") || source.contains("legend")) {
            return "folklore";
        }
        return "crafts";
    }

    private record MediaSummaryResult(String mediaValue, boolean summaryOnly) {
    }
}