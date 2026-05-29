package com.group32.cpt202.zyl_project.zyl_display;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group32.cpt202.LY_heritage.dto.HeritageCommentDTO;
import com.group32.cpt202.LY_heritage.dto.MessageCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web layer tests for {@link HeritageDisplayController}.
 */
@ExtendWith(MockitoExtension.class)
class HeritageDisplayControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private HeritageDisplayService service;

    @InjectMocks
    private HeritageDisplayController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("TC-API-HOM-01: GET /api/zyl/display/home returns summary")
    void getHomeSummary() throws Exception {
        HomeSummary summary = new HomeSummary();
        summary.setApprovedHeritageCount(5);
        summary.setContributorCount(3);
        summary.setPendingApplicationCount(2);
        summary.setCommentCount(7);
        summary.setLatestHeritages(List.of());

        when(service.getHomeSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/zyl/display/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvedHeritageCount").value(5))
                .andExpect(jsonPath("$.contributorCount").value(3));
    }

    @Test
    @DisplayName("TC-API-CRT-01: POST /api/zyl/display creates community post")
    void createCommunityPost() throws Exception {
        HeritageDisplay display = new HeritageDisplay();
        display.setId(100L);
        display.setTitle("Temple craft");

        when(service.createCommunityPost(any(CommunityPostCreateRequest.class))).thenReturn(display);

        CommunityPostCreateRequest request = new CommunityPostCreateRequest();
        request.setUserId(10L);
        request.setTitle("Temple craft");
        request.setDescription("Description");
        request.setImageUrl("https://example.com/image.png");

        mockMvc.perform(post("/api/zyl/display")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Temple craft"));
    }

    @Test
    @DisplayName("TC-API-LIK-01: PUT /api/zyl/display/{id}/like toggles like")
    void toggleLike() throws Exception {
        HeritageDisplay display = new HeritageDisplay();
        display.setId(100L);
        display.setLikeCount(1L);
        display.setLikedByCurrentUser(true);

        when(service.toggleCommunityPostLike(100L, 10L)).thenReturn(display);

        mockMvc.perform(put("/api/zyl/display/100/like").param("userId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likeCount").value(1))
                .andExpect(jsonPath("$.likedByCurrentUser").value(true));
    }

    @Test
    @DisplayName("TC-API-DEL-01: DELETE /api/zyl/display/{id} removes post")
    void deleteCommunityPost() throws Exception {
        doNothing().when(service).deleteCommunityPost(100L, 10L);

        mockMvc.perform(delete("/api/zyl/display/100").param("userId", "10"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("TC-API-CMT-01: GET /api/zyl/display/{id}/comments returns comment tree")
    void getComments() throws Exception {
        HeritageCommentDTO comment = new HeritageCommentDTO();
        comment.setId(1L);
        comment.setContent("Nice post");
        comment.setUsername("alice");

        when(service.getCommunityComments(100L)).thenReturn(List.of(comment));

        mockMvc.perform(get("/api/zyl/display/100/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Nice post"))
                .andExpect(jsonPath("$[0].username").value("alice"));
    }

    @Test
    @DisplayName("TC-API-ALL-01: GET /api/zyl/display/all returns community feed")
    void getAll() throws Exception {
        HeritageDisplay display = new HeritageDisplay();
        display.setId(100L);
        display.setTitle("Feed item");

        when(service.getAll(isNull())).thenReturn(List.of(display));

        mockMvc.perform(get("/api/zyl/display/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Feed item"));
    }

    @Test
    @DisplayName("TC-API-PLT-01: GET /api/zyl/display/platform returns approved items")
    void getPlatformApproved() throws Exception {
        HeritageDisplay display = new HeritageDisplay();
        display.setId(1L);
        display.setTitle("Platform item");
        display.setPlatformPublished(true);

        when(service.getPlatformApproved()).thenReturn(List.of(display));

        mockMvc.perform(get("/api/zyl/display/platform"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Platform item"));
    }

    @Test
    @DisplayName("TC-API-CAT-01: GET /api/zyl/display/category/{category} filters feed")
    void getByCategory() throws Exception {
        HeritageDisplay display = new HeritageDisplay();
        display.setId(100L);
        display.setTitle("Temple");
        display.setCategory("architecture");

        when(service.getByCategory("architecture", null)).thenReturn(List.of(display));

        mockMvc.perform(get("/api/zyl/display/category/architecture"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("architecture"));
    }

    @Test
    @DisplayName("TC-API-SHR-01: POST /api/zyl/display/{id}/share increments share count")
    void incrementShare() throws Exception {
        HeritageDisplay display = new HeritageDisplay();
        display.setId(100L);
        display.setShareCount(3L);

        when(service.incrementCommunityPostShare(100L, 10L)).thenReturn(display);

        mockMvc.perform(post("/api/zyl/display/100/share").param("userId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shareCount").value(3));
    }

    @Test
    @DisplayName("TC-API-CMT-02: POST /api/zyl/display/{id}/comments adds comment")
    void addComment() throws Exception {
        HeritageCommentDTO comment = new HeritageCommentDTO();
        comment.setId(1L);
        comment.setContent("Great post");

        when(service.addCommunityComment(100L, 10L, "Great post", null))
                .thenReturn(List.of(comment));

        MessageCreateRequest request = new MessageCreateRequest();
        request.setUserId(10L);
        request.setContent("Great post");

        mockMvc.perform(post("/api/zyl/display/100/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Great post"));
    }

    @Test
    @DisplayName("TC-API-GET-01: GET /api/zyl/display/{id} returns single post")
    void getById() throws Exception {
        HeritageDisplay display = new HeritageDisplay();
        display.setId(100L);
        display.setTitle("Single post");

        when(service.getById(100L, 10L)).thenReturn(display);

        mockMvc.perform(get("/api/zyl/display/100").param("userId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Single post"));
    }
}
