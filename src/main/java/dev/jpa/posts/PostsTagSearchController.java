package dev.jpa.posts;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts/search")
public class PostsTagSearchController {

    @Autowired
    private PostsService postsService;

    /**
     * 태그 다중 검색
     * GET http://localhost:9100/posts/search/tags?tags=여행&tags=서울&mode=AND&page=0&size=10

     */
    @GetMapping("/tags")
    public ResponseEntity<PageResponse<Posts>> searchByTags(
        @RequestParam(name = "tags", required = false) List<String> tags,
            @RequestParam(name = "mode", defaultValue = "AND") String mode,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "cnt") // ✅ 게시물 조회수 기준
        );

        Page<Posts> result =
                postsService.searchByTags(tags, mode, pageable);

        return ResponseEntity.ok(
                new PageResponse<>(
                        result.getContent(),
                        result.getNumber(),
                        result.getSize(),
                        result.getTotalElements(),
                        result.getTotalPages()
                )
        );
    }
}
