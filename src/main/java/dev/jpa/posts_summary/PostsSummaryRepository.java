package dev.jpa.posts_summary;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PostsSummaryRepository extends JpaRepository<PostsSummary, Long> {
}
