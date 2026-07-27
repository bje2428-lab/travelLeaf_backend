package dev.jpa.ai.user_profile_embedding;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import dev.jpa.posts_embeddings.PostsEmbedding;
import dev.jpa.posts_embeddings.PostsEmbeddingRepository;

@Service
@RequiredArgsConstructor
public class UserRecommendationService {

    private final UserProfileEmbeddingRepository userEmbeddingRepo;
    private final PostsEmbeddingRepository postsEmbeddingRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Long> recommendPosts(String userId) throws Exception {

        String userVectorJson = userEmbeddingRepo.findEmbedding(userId);
        if (userVectorJson == null) {
            throw new RuntimeException("No user embedding");
        }

        List<Double> userVector = objectMapper.readValue(
                userVectorJson, new TypeReference<>() {}
        );

        List<PostsEmbedding> posts = postsEmbeddingRepo.findAll();

        return posts.stream()
                .map(p -> {
                    try {
                        List<Double> vec = objectMapper.readValue(
                                p.getEmbedding(), new TypeReference<>() {}
                        );
                        double score = VectorUtils.cosineSimilarity(userVector, vec);
                        return Map.entry(p.getPostsId(), score);   // ← 너 테이블 필드명
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .sorted((a,b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(10)
                .map(Map.Entry::getKey)
                .toList();
    }
}
