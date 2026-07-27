package dev.jpa.posts_summary;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostsSummaryService {

    private final PostsSummaryRepository repository;

    public PostsSummaryDTO getByPostId(Long postId) {
        return repository.findById(postId)
                .map(e -> new PostsSummaryDTO(
                        e.getPostId(),
                        e.getSummary(),
                        e.getKeywords()
                ))
                .orElse(null);
    }

    public void save(PostsSummaryDTO dto) {
        PostsSummary entity = new PostsSummary();
        entity.setPostId(dto.getPostId());
        entity.setSummary(dto.getSummary());
        entity.setKeywords(dto.getKeywords());

        repository.save(entity);
    }
}
