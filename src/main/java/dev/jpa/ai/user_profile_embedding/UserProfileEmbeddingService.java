package dev.jpa.ai.user_profile_embedding;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.posts_embeddings.EmbeddingAiClient;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserProfileEmbeddingService {

    private final UserProfileRepository profileRepository;
    private final UserProfileEmbeddingRepository embeddingRepository;
    private final EmbeddingAiClient aiClient;

    @Transactional
    public void updateUserEmbedding(String userId) {

        String profileText = profileRepository.getUserProfileText(userId);
        if (profileText == null || profileText.isBlank()) return;

        String embedding = aiClient.createEmbedding(profileText);

        embeddingRepository.saveOrUpdate(userId, embedding);
    }
}
