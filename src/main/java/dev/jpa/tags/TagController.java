package dev.jpa.tags;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    // 서비스 주입
    private final TagService tagService;

    /**
     * ==================================================
     * 1. 태그 생성 (POST)
     * ==================================================
     * ■ URL(POSTMAN): http://localhost:9100/api/tags
     * ■ METHOD: POST
     * ■ BODY (raw / JSON):
     * {
     *   "name": "여행"
     * }
     * ■ RESPONSE (예시)
     * {
     *   "tagId": 1,
     *   "name": "여행"
     * }
     */
    @PostMapping
    public ResponseEntity<TagDTO> createTag(@Valid @RequestBody TagDTO dto) {
        TagDTO created = tagService.createTag(dto);
        return ResponseEntity.created(URI.create("/api/tags/" + created.getTagId()))
                             .body(created);
    }

    /**
     * ==================================================
     * 2. 태그 전체 조회 + 검색 기능 (GET)
     * ==================================================
     * ■ URL(전체): http://localhost:9100/api/tags
     * ■ URL(검색): http://localhost:9100/api/tags?q=여행
     * ■ METHOD: GET
     * 
     * ■ RESPONSE (예시):
     * [
     *   { "tagId": 1, "name": "여행" },
     *   { "tagId": 2, "name": "국내여행" }
     * ]
     */
    @GetMapping
    public ResponseEntity<List<TagDTO>> getAll(@RequestParam(value = "q", required = false) String q) {
        if (q != null && !q.isBlank()) {
            return ResponseEntity.ok(tagService.searchTags(q));
        }
        return ResponseEntity.ok(tagService.getAllTags());
    }

    /**
     * ==================================================
     * 3. 특정 태그 조회 (GET)
     * ==================================================
     * ■ URL: http://localhost:9100/api/tags/{id}
     * 예) http://localhost:9100/api/tags/1
     * ■ METHOD: GET
     * 
     * ■ RESPONSE (예시):
     * {
     *   "tagId": 1,
     *   "name": "여행"
     * }
     */
    @GetMapping("/{id}")
    public ResponseEntity<TagDTO> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(tagService.getTag(id));
    }

    /**
     * ==================================================
     * 4. 태그 수정 (PUT)
     * ==================================================
     * ■ URL: http://localhost:9100/api/tags/{id}
     * 예) http://localhost:9100/api/tags/1
     * ■ METHOD: PUT
     * ■ BODY (raw / JSON):
     * {
     *   "name": "국내여행"
     * }
     * 
     * ■ RESPONSE (예시):
     * {
     *   "tagId": 1,
     *   "name": "국내여행"
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<TagDTO> update(@PathVariable Long id, @Valid @RequestBody TagDTO dto) {
        return ResponseEntity.ok(tagService.updateTag(id, dto));
    }

    /**
     * ==================================================
     * 5. 태그 삭제 (DELETE)
     * ==================================================
     * ■ URL: http://localhost:9100/api/tags/{id}
     * 예) http://localhost:9100/api/tags/1
     * ■ METHOD: DELETE
     * 
     * ■ RESPONSE: 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
    /**
     * 태그 자동완성
     * GET http://localhost:9100/api/tags/autocomplete?keyword=여
     */
    @GetMapping("/autocomplete")
    public ResponseEntity<List<TagDTO>> autocomplete(
            @RequestParam(name = "keyword") String keyword
    ) {
        return ResponseEntity.ok(tagService.autocomplete(keyword));
    }

    /**
     * 인기 태그 조회
     * GET http://localhost:9100/api/tags/popular?limit=10
     */
    @GetMapping("/popular")
    public ResponseEntity<List<TagPopularDTO>> popular(
            @RequestParam(name = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(
                tagService.getPopularTags(limit)
        );
    }

    }


