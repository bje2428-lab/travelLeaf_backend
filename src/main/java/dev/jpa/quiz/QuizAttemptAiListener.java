package dev.jpa.quiz;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
@RequiredArgsConstructor
public class QuizAttemptAiListener {

    private final QuizAttemptAiJobService jobService;

    // ✅ 트랜잭션 COMMIT 이후에 실행 (DB에 attempt가 확실히 저장된 뒤)
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAttemptCreated(QuizAttemptCreatedEvent e) {
        jobService.process(e.attemptId(), e.quizId(), e.selectedNo(), e.correct());
    }
}
