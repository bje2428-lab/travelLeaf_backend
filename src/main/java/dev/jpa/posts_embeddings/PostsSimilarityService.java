package dev.jpa.posts_embeddings;

import dev.jpa.posts.PostsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostsSimilarityService {

    private final PostsEmbeddingRepository repo;
    private final PostsRepository postsRepository;
    private final EmbeddingParser parser;
    private final CosineSimilarity cosine;

    public List<SimilarPostDTO> findSimilarPosts(long targetPostId) {

        // 1️⃣ 타깃 게시글 임베딩
        PostsEmbedding target = repo.findById(targetPostId)
                .orElseThrow(() -> new RuntimeException("타깃 게시글 임베딩 없음"));

        String raw = target.getEmbedding();
        if (raw == null) {
            throw new RuntimeException("이 게시글은 AI 임베딩이 생성되지 않았습니다.");
        }

        // 🔥 형식 상관없이 파싱 시도
        List<Double> targetVec;
        try {
            targetVec = parser.parseVector(raw);
        } catch (Exception e) {
            throw new RuntimeException("타깃 게시글 임베딩 파싱 실패");
        }

        // 2️⃣ 카테고리
        long cateno = postsRepository.findCatenoByPostId(targetPostId);
        if (cateno == 0) {
            throw new RuntimeException("해당 게시글의 카테고리를 찾을 수 없습니다.");
        }

        // 3️⃣ 같은 카테고리 글들과 비교
        return repo.findByCategory(cateno).stream()
                .filter(e -> !e.getPostsId().equals(targetPostId))
                .filter(e -> e.getEmbedding() != null)
                .map(e -> {
                    try {
                        List<Double> vec = parser.parseVector(e.getEmbedding());

                        // 🔥 벡터 길이 다르면 비교 불가 → 버림
                        if (vec.size() != targetVec.size()) return null;

                        double score = cosine.similarity(targetVec, vec);
                        return new SimilarPostDTO(e.getPostsId(), score);
                    } catch (Exception ex) {
                        // 파싱 실패, 깨진 임베딩 → 무시
                        return null;
                    }
                })
                .filter(e -> e != null)
                .filter(e -> e.getScore() >= 0.3)   // ⭐ 임계치 유지
                .sorted(Comparator.comparingDouble(SimilarPostDTO::getScore).reversed())
                .limit(5)
                .toList();
    }
}
