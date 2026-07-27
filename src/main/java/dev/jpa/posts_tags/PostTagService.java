package dev.jpa.posts_tags;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.posts.PageResponse;
import dev.jpa.posts.Posts;
import dev.jpa.posts.PostsDTO;
import dev.jpa.posts.PostsRepository;
import dev.jpa.tags.Tag;
import dev.jpa.tags.TagRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostTagService {

    private final PostTagRepository postTagRepository;
    private final PostsRepository postsRepository;
    private final TagRepository tagRepository;

    /* =========================
       게시글의 태그 조회
    ========================= */
    @Transactional(readOnly = true)
    public List<PostTagResponseDTO> getTagsByPost(long postId) {

        List<PostTag> list =
                postTagRepository.findByPostIdWithTag(postId);

        if (list.isEmpty()) {
            return List.of();
        }

        return list.stream()
                .map(pt -> new PostTagResponseDTO(
                        pt.getTag().getTagId(),
                        pt.getTag().getName()
                ))
                .toList();
    }

    /* =========================
       🔥 태그별 게시글 조회 (페이징)
    ========================= */
    @Transactional(readOnly = true)
    public PageResponse<PostsDTO> getPostsByTag(
            long tagId,
            Pageable pageable
    ) {
        Page<Posts> page =
                postTagRepository.findPostsByTagId(tagId, pageable);

        // 🔥 Page<Posts> → Page<PostsDTO>
        Page<PostsDTO> dtoPage = page.map(PostsDTO::new);

        // 🔥 PageResponse 직접 생성 (of() 사용 안 함)
        return new PageResponse<>(
                dtoPage.getContent(),
                dtoPage.getNumber(),
                dtoPage.getSize(),
                dtoPage.getTotalElements(),
                dtoPage.getTotalPages()
        );
    }

    /* =========================
       게시글 + 태그 연결
    ========================= */
    @Transactional
    public PostTag addTagToPost(long postId, long tagId) {

        Posts post = postsRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("태그 없음"));

        PostTagId id = new PostTagId(postId, tagId);

        PostTag postTag = PostTag.builder()
                .id(id)
                .post(post)
                .tag(tag)
                .build();

        return postTagRepository.save(postTag);
    }

    /* =========================
       게시글 + 태그 연결 삭제
    ========================= */
    @Transactional
    public void removeTagFromPost(long postId, long tagId) {
        postTagRepository.deleteById(new PostTagId(postId, tagId));
    }
    /**
     * 🔥 게시글의 태그 전체 교체 (등록/수정 공용)
     */
    @Transactional
    public void replaceTags(long postId, List<String> tags) {

        // 1️⃣ 기존 태그 매핑 전부 삭제
        postTagRepository.deleteByPost_PostId(postId);

        if (tags == null || tags.isEmpty()) return;

        Posts post = postsRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        // 2️⃣ 태그 생성 / 조회 후 매핑
        for (String raw : tags) {
            String name = normalize(raw);

            Tag tag = tagRepository.findByName(name)
                    .orElseGet(() ->
                        tagRepository.save(
                            Tag.builder().name(name).build()
                        )
                    );

            PostTagId id = new PostTagId(postId, tag.getTagId());

            PostTag postTag = PostTag.builder()
                    .id(id)
                    .post(post)
                    .tag(tag)
                    .build();

            postTagRepository.save(postTag);
        }
    }

    private String normalize(String tag) {
        return tag.trim().toLowerCase().replace("#", "");
    }
}
