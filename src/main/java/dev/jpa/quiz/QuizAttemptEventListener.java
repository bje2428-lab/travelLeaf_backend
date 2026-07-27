package dev.jpa.quiz;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class QuizAttemptEventListener {

    private final QuizAttemptAiJobService jobService;

    // ✅ 트랜잭션 커밋 후 실행 + 비동기
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(QuizAttemptCreatedEvent event) {
        jobService.process(
                event.attemptId(),
                event.quizId(),
                event.selectedNo(),
                event.correct()
        );
    }
}
