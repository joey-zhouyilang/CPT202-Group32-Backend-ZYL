package com.group32.cpt202.zyl_project.zyl_display;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link HeritageDisplayService} (zyl display / community module).
 */
@ExtendWith(MockitoExtension.class)
class HeritageDisplayServiceTest {

    private static final String VALID_IMAGE =
            "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==";

    @Mock
    private HeritageDisplayRepository repository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ContributorApplicationRepository contributorApplicationRepository;
    @Mock
    private HeritageItemRepository heritageItemRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private ForumPostLikeRepository forumPostLikeRepository;

    @InjectMocks
    private HeritageDisplayService service;

    private User author;
    private Message communityPost;

    @BeforeEach
    void setUp() {
        author = new User();
        author.setId(10L);
        author.setUsername("contributor");
        author.setAvatarUrl("avatar.png");

        communityPost = new Message();
        communityPost.setId(100L);
        communityPost.setUserId(10L);
        communityPost.setTitle("Temple craft");
        communityPost.setContent("A beautiful temple building");
        communityPost.setTags("architecture");
        communityPost.setImageUrl(VALID_IMAGE);
        communityPost.setLikeCount(0L);
        communityPost.setShareCount(0L);
        communityPost.setSentAt(LocalDateTime.of(2026, 5, 10, 12, 0));
    }

    @Nested
    @DisplayName("community feed queries")
    class FeedQueryTests {

        @Test
        @DisplayName("TC-FED-01: getAll returns community posts")
        void getAll_success() {
            when(messageRepository.findByForumPostIdIsNullAndHeritageIdIsNullAndTitleIsNotNullOrderBySentAtDesc())
                    .thenReturn(List.of(communityPost));
            when(userRepository.findAllById(anyList())).thenReturn(List.of(author));
            when(messageRepository.findByForumPostIdIn(anyList())).thenReturn(List.of());
            when(forumPostLikeRepository.findByUserIdAndPostIdIn(eq(10L), anyList())).thenReturn(List.of());

            List<HeritageDisplay> displays = service.getAll(10L);

            assertEquals(1, displays.size());
            assertEquals("Temple craft", displays.get(0).getTitle());
        }

        @Test
        @DisplayName("TC-FED-02: getByCategory filters by category")
        void getByCategory_success() {
            when(messageRepository.findByForumPostIdIsNullAndHeritageIdIsNullAndTitleIsNotNullOrderBySentAtDesc())
                    .thenReturn(List.of(communityPost));
            when(userRepository.findAllById(anyList())).thenReturn(List.of(author));
            when(messageRepository.findByForumPostIdIn(anyList())).thenReturn(List.of());

            List<HeritageDisplay> displays = service.getByCategory("architecture", null);

            assertEquals(1, displays.size());
            assertEquals("architecture", displays.get(0).getCategory());
        }

        @Test
        @DisplayName("TC-FED-03: getById returns enriched post")
        void getById_success() {
            when(messageRepository.findById(100L)).thenReturn(Optional.of(communityPost));
            when(userRepository.findAllById(anyList())).thenReturn(List.of(author));
            when(messageRepository.findByForumPostIdIn(anyList())).thenReturn(List.of());
            when(forumPostLikeRepository.findByUserIdAndPostIdIn(eq(10L), anyList())).thenReturn(List.of());

            HeritageDisplay display = service.getById(100L, 10L);

            assertEquals(100L, display.getId());
            assertEquals("contributor", display.getContributorName());
        }
    }

    @Nested
    @DisplayName("getHomeSummary")
    class HomeSummaryTests {

        @Test
        @DisplayName("TC-HOM-01: aggregates dashboard counters")
        void getHomeSummary_success() {
            when(heritageItemRepository.countByStatusIgnoreCase("APPROVED")).thenReturn(5L);
            when(userRepository.countByRole(User.Role.CONTRIBUTOR)).thenReturn(3L);
            when(contributorApplicationRepository.countByStatus(ContributorApplication.Status.PENDING)).thenReturn(2L);
            when(messageRepository.countByHeritageIdIsNotNull()).thenReturn(7L);
            when(repository.findRecentApproved(6)).thenReturn(List.of());

            HomeSummary summary = service.getHomeSummary();

            assertEquals(5L, summary.getApprovedHeritageCount());
            assertEquals(3L, summary.getContributorCount());
            assertEquals(2L, summary.getPendingApplicationCount());
            assertEquals(7L, summary.getCommentCount());
            assertNotNull(summary.getLatestHeritages());
        }
    }

    @Nested
    @DisplayName("createCommunityPost")
    class CreatePostTests {

        @Test
        @DisplayName("TC-CRT-01: creates a community post")
        void createCommunityPost_success() {
            CommunityPostCreateRequest request = validCreateRequest();

            when(userRepository.findById(10L)).thenReturn(Optional.of(author));
            when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
                Message saved = invocation.getArgument(0);
                saved.setId(100L);
                return saved;
            });
            when(userRepository.findAllById(anyList())).thenReturn(List.of(author));
            when(messageRepository.findByForumPostIdIn(anyList())).thenReturn(List.of());
            when(forumPostLikeRepository.findByUserIdAndPostIdIn(eq(10L), anyList())).thenReturn(List.of());

            HeritageDisplay display = service.createCommunityPost(request);

            assertEquals(100L, display.getId());
            assertEquals("Temple craft", display.getTitle());
            assertEquals("contributor", display.getContributorName());
            assertEquals("architecture", display.getCategory());
            assertFalse(Boolean.TRUE.equals(display.getPlatformPublished()));

            ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
            verify(messageRepository).save(captor.capture());
            assertEquals("Temple craft", captor.getValue().getTitle());
            assertEquals(10L, captor.getValue().getUserId());
        }

        @Test
        @DisplayName("TC-CRT-02: rejects null request")
        void createCommunityPost_nullRequest() {
            RuntimeException ex = assertThrows(RuntimeException.class, () -> service.createCommunityPost(null));
            assertEquals("request is required", ex.getMessage());
        }

        @Test
        @DisplayName("TC-CRT-03: rejects missing title")
        void createCommunityPost_missingTitle() {
            CommunityPostCreateRequest request = validCreateRequest();
            request.setTitle("   ");

            RuntimeException ex = assertThrows(RuntimeException.class, () -> service.createCommunityPost(request));
            assertEquals("title is required", ex.getMessage());
        }

        @Test
        @DisplayName("TC-CRT-04: rejects PDF media")
        void createCommunityPost_pdfNotAllowed() {
            CommunityPostCreateRequest request = validCreateRequest();
            request.setImageUrl("https://example.com/file.pdf");

            RuntimeException ex = assertThrows(RuntimeException.class, () -> service.createCommunityPost(request));
            assertEquals("PDF files are not supported in community posts", ex.getMessage());
        }

        @Test
        @DisplayName("TC-CRT-05: rejects unknown author")
        void createCommunityPost_userNotFound() {
            CommunityPostCreateRequest request = validCreateRequest();
            when(userRepository.findById(10L)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class, () -> service.createCommunityPost(request));
            assertEquals("user not found", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("deleteCommunityPost")
    class DeletePostTests {

        @Test
        @DisplayName("TC-DEL-01: author can delete own post")
        void deleteCommunityPost_success() {
            when(messageRepository.findById(100L)).thenReturn(Optional.of(communityPost));

            service.deleteCommunityPost(100L, 10L);

            verify(forumPostLikeRepository).deleteByPostId(100L);
            verify(messageRepository).deleteByForumPostId(100L);
            verify(messageRepository).delete(communityPost);
        }

        @Test
        @DisplayName("TC-DEL-02: non-author cannot delete post")
        void deleteCommunityPost_notAuthor() {
            when(messageRepository.findById(100L)).thenReturn(Optional.of(communityPost));

            RuntimeException ex = assertThrows(RuntimeException.class, () -> service.deleteCommunityPost(100L, 99L));
            assertEquals("only the post author can delete this post", ex.getMessage());
            verify(messageRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("toggleCommunityPostLike")
    class LikeTests {

        @Test
        @DisplayName("TC-LIK-01: adds like when not previously liked")
        void toggleLike_add() {
            when(messageRepository.findById(100L)).thenReturn(Optional.of(communityPost));
            when(userRepository.findById(10L)).thenReturn(Optional.of(author));
            when(forumPostLikeRepository.findByPostIdAndUserId(100L, 10L)).thenReturn(Optional.empty());
            when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(userRepository.findAllById(anyList())).thenReturn(List.of(author));
            when(messageRepository.findByForumPostIdIn(anyList())).thenReturn(List.of());
            when(forumPostLikeRepository.findByUserIdAndPostIdIn(eq(10L), anyList())).thenReturn(List.of());

            HeritageDisplay display = service.toggleCommunityPostLike(100L, 10L);

            assertEquals(1L, display.getLikeCount());
            verify(forumPostLikeRepository).save(any(ForumPostLike.class));
        }

        @Test
        @DisplayName("TC-LIK-02: removes like when already liked")
        void toggleLike_remove() {
            ForumPostLike existingLike = new ForumPostLike();
            existingLike.setPostId(100L);
            existingLike.setUserId(10L);
            communityPost.setLikeCount(1L);

            when(messageRepository.findById(100L)).thenReturn(Optional.of(communityPost));
            when(userRepository.findById(10L)).thenReturn(Optional.of(author));
            when(forumPostLikeRepository.findByPostIdAndUserId(100L, 10L)).thenReturn(Optional.of(existingLike));
            when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(userRepository.findAllById(anyList())).thenReturn(List.of(author));
            when(messageRepository.findByForumPostIdIn(anyList())).thenReturn(List.of());
            when(forumPostLikeRepository.findByUserIdAndPostIdIn(eq(10L), anyList())).thenReturn(List.of());

            HeritageDisplay display = service.toggleCommunityPostLike(100L, 10L);

            assertEquals(0L, display.getLikeCount());
            verify(forumPostLikeRepository).delete(existingLike);
        }
    }

    @Nested
    @DisplayName("incrementCommunityPostShare")
    class ShareTests {

        @Test
        @DisplayName("TC-SHR-01: increments share count")
        void incrementShare_success() {
            when(messageRepository.findById(100L)).thenReturn(Optional.of(communityPost));
            when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(userRepository.findAllById(anyList())).thenReturn(List.of(author));
            when(messageRepository.findByForumPostIdIn(anyList())).thenReturn(List.of());

            HeritageDisplay display = service.incrementCommunityPostShare(100L, null);

            assertEquals(1L, display.getShareCount());
        }
    }

    @Nested
    @DisplayName("community comments")
    class CommentTests {

        @Test
        @DisplayName("TC-CMT-01: adds a top-level comment")
        void addCommunityComment_success() {
            when(messageRepository.findById(100L)).thenReturn(Optional.of(communityPost));
            when(userRepository.findById(10L)).thenReturn(Optional.of(author));
            when(messageRepository.findByForumPostIdOrderBySentAtAsc(100L)).thenReturn(List.of());

            List<HeritageCommentDTO> comments =
                    service.addCommunityComment(100L, 10L, "Great post", null);

            assertNotNull(comments);
            verify(messageRepository).save(any(Message.class));
        }

        @Test
        @DisplayName("TC-CMT-02: rejects invalid parent comment")
        void addCommunityComment_invalidParent() {
            when(messageRepository.findById(100L)).thenReturn(Optional.of(communityPost));
            when(userRepository.findById(10L)).thenReturn(Optional.of(author));
            when(messageRepository.findById(999L)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(
                    RuntimeException.class,
                    () -> service.addCommunityComment(100L, 10L, "reply", 999L)
            );
            assertEquals("parent comment not found", ex.getMessage());
        }

        @Test
        @DisplayName("TC-CMT-03: builds nested comment tree")
        void getCommunityComments_tree() {
            Message reply = new Message();
            reply.setId(2L);
            reply.setForumPostId(100L);
            reply.setParentMessageId(1L);
            reply.setUserId(10L);
            reply.setContent("Reply");
            reply.setSentAt(LocalDateTime.of(2026, 5, 10, 13, 0));

            Message top = new Message();
            top.setId(1L);
            top.setForumPostId(100L);
            top.setUserId(10L);
            top.setContent("Top");
            top.setSentAt(LocalDateTime.of(2026, 5, 10, 12, 30));

            when(messageRepository.findById(100L)).thenReturn(Optional.of(communityPost));
            when(messageRepository.findByForumPostIdOrderBySentAtAsc(100L)).thenReturn(List.of(top, reply));
            when(userRepository.findAllById(anyList())).thenReturn(List.of(author));

            List<HeritageCommentDTO> comments = service.getCommunityComments(100L);

            assertEquals(1, comments.size());
            assertEquals("Top", comments.get(0).getContent());
            assertEquals(1, comments.get(0).getReplies().size());
            assertEquals("Reply", comments.get(0).getReplies().get(0).getContent());
            assertEquals("contributor", comments.get(0).getReplies().get(0).getReplyToUsername());
        }
    }

    @Nested
    @DisplayName("platform heritage")
    class PlatformTests {

        @Test
        @DisplayName("TC-PLT-01: returns approved platform item")
        void getPlatformById_success() {
            HeritageDisplay display = new HeritageDisplay();
            display.setId(1L);
            display.setTitle("Approved item");
            display.setPlatformPublished(true);

            when(repository.findPlatformApprovedById(1L)).thenReturn(display);

            HeritageDisplay result = service.getPlatformById(1L);

            assertEquals("Approved item", result.getTitle());
            assertTrue(Boolean.TRUE.equals(result.getPlatformPublished()));
        }

        @Test
        @DisplayName("TC-PLT-02: rejects missing platform item")
        void getPlatformById_notFound() {
            when(repository.findPlatformApprovedById(99L)).thenReturn(null);

            RuntimeException ex = assertThrows(RuntimeException.class, () -> service.getPlatformById(99L));
            assertEquals("platform heritage not found", ex.getMessage());
        }
    }

    private CommunityPostCreateRequest validCreateRequest() {
        CommunityPostCreateRequest request = new CommunityPostCreateRequest();
        request.setUserId(10L);
        request.setTitle("Temple craft");
        request.setDescription("A beautiful temple building");
        request.setTags("architecture");
        request.setImageUrl(VALID_IMAGE);
        return request;
    }
}
