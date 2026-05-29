package com.group32.cpt202.zyl_project.zyl_display;

import com.group32.cpt202.LY_heritage.dto.HeritageCommentDTO;
import com.group32.cpt202.LY_heritage.dto.MessageCreateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// REST 控制器，暴露 HTTP API 接口。
@RestController

// 允许前端跨域访问该控制器。
/**
 * HeritageDisplayController 类，处理当前模块的业务逻辑或数据结构。
 */
@CrossOrigin
@RequestMapping("/api/zyl/display")
public class HeritageDisplayController {

    private final HeritageDisplayService service;

    public HeritageDisplayController(HeritageDisplayService service) {
        this.service = service;
    }

    @GetMapping("/home")
    public ResponseEntity<HomeSummary> getHomeSummary() {
        return ResponseEntity.ok(service.getHomeSummary());
    }

    @GetMapping("/all")
    public ResponseEntity<List<HeritageDisplay>> getAll(@RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(service.getAll(userId));
    }

    @GetMapping("/platform")
    public ResponseEntity<List<HeritageDisplay>> getPlatformApproved() {
        return ResponseEntity.ok(service.getPlatformApproved());
    }

    @GetMapping("/platform/{id}")
    public ResponseEntity<HeritageDisplay> getPlatformById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getPlatformById(id));
    }

// POST 请求处理方法。
    @PostMapping
    public ResponseEntity<HeritageDisplay> create(@RequestBody CommunityPostCreateRequest request) {
        return ResponseEntity.ok(service.createCommunityPost(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HeritageDisplay> getById(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(service.getById(id, userId));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<HeritageCommentDTO>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(service.getCommunityComments(id));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<List<HeritageCommentDTO>> addComment(@PathVariable Long id, @RequestBody MessageCreateRequest request) {
        return ResponseEntity.ok(service.addCommunityComment(id, request.getUserId(), request.getContent(), request.getParentMessageId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam Long userId) {
        service.deleteCommunityPost(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/like")
    public ResponseEntity<HeritageDisplay> toggleLike(@PathVariable Long id, @RequestParam Long userId) {
        return ResponseEntity.ok(service.toggleCommunityPostLike(id, userId));
    }

    @PostMapping("/{id}/share")
    public ResponseEntity<HeritageDisplay> incrementShare(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(service.incrementCommunityPostShare(id, userId));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<HeritageDisplay>> getByCategory(@PathVariable String category,
                                                               @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(service.getByCategory(category, userId));
    }

    @GetMapping("/platform/category/{category}")
    public ResponseEntity<List<HeritageDisplay>> getPlatformByCategory(@PathVariable String category) {
        return ResponseEntity.ok(service.getPlatformApprovedByCategory(category));
    }
}