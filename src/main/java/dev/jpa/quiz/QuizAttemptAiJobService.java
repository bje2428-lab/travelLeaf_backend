package dev.jpa.quiz;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizAttemptAiJobService {

    private final QuizRepository quizRepo;
    private final QuizAttemptRepository attemptRepo;
    private final QuizAiService quizAiService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(Long attemptId, Long quizId, int selectedNo, boolean correct) {

        QuizAttempt attempt = attemptRepo.findById(attemptId).orElse(null);
        Quiz quiz = quizRepo.findById(quizId).orElse(null);
        if (attempt == null || quiz == null) return;

        try {
            // ✅ 상태: PENDING(기본) -> 작업 시작
            attempt.setAiStatus("PENDING");
            attempt.setAiUpdatedAt(new Date());
            attemptRepo.save(attempt);

            // 1) 후보 문제(같은 카테고리)
            List<Quiz> candidates = quizRepo.findAll().stream()
                    .filter(q -> q.getQuizId() != null)
                    .filter(q -> !q.getQuizId().equals(quiz.getQuizId()))
                    .filter(q -> Objects.equals(q.getCategory(), quiz.getCategory()))
                    .limit(10)
                    .toList();

            // 2) 코치/추천이유 생성
            QuizAttemptAiResult ai = quizAiService.buildAttemptAi(
                    quiz, selectedNo, correct, candidates
            );

            // 3) 응용문제 3개 생성 (너가 이미 만들어둔 메서드 기준)
            List<PracticeQuizDTO> generated = quizAiService.generatePracticeQuizzes(
                    quiz, correct, selectedNo
            );

            String generatedIdsCsv = "";
            if (generated != null && !generated.isEmpty()) {
                List<Long> newIds = generated.stream().limit(3).map(g -> {
                    Quiz nq = new Quiz();
                    nq.setCategory(quiz.getCategory());
                    nq.setQuestion(g.getQuestion());
                    nq.setOption1(g.getOption1());
                    nq.setOption2(g.getOption2());
                    nq.setOption3(g.getOption3());
                    nq.setOption4(g.getOption4());
                    nq.setCorrectNo(g.getCorrectNo());
                    nq.setExplanation(g.getExplanation());
                    nq.setExpReward(quiz.getExpReward());
                    nq.setCreatedAt(new Date());
                    quizRepo.save(nq);
                    return nq.getQuizId();
                }).toList();

                generatedIdsCsv = newIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
            }

            // 4) attempt 업데이트
            attempt.setCoachText(ai.getCoachText());
            attempt.setStrengthText(ai.getStrengthText());
            attempt.setImproveText(ai.getImproveText());
            attempt.setNextActionText(ai.getNextActionText());

            if (!generatedIdsCsv.isBlank()) {
                attempt.setRecommendQuizIds(generatedIdsCsv);
                attempt.setRecommendReason("방금 문제를 응용한 연습문제 3개를 생성했어요.");
            } else {
                attempt.setRecommendQuizIds(ai.getRecommendQuizIds());
                attempt.setRecommendReason(ai.getRecommendReason());
            }

            attempt.setAiStatus("DONE");
            attempt.setAiUpdatedAt(new Date());

            attemptRepo.save(attempt);

        } catch (Exception ex) {
            attempt.setAiStatus("FAILED");
            attempt.setAiUpdatedAt(new Date());
            attemptRepo.save(attempt);
        }
    }
}
