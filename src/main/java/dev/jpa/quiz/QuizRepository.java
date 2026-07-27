package dev.jpa.quiz;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    // ✅ 추천 ids(csv)로 상세조회할 때 사용
    List<Quiz> findByQuizIdIn(List<Long> ids);
}
