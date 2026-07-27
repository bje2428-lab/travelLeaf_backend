package dev.jpa.quiz;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttemptAiResult {
    private String coachText;
    private String recommendQuizIds;
    private String recommendReason;
    private String strengthText;
    private String improveText;
    private String nextActionText;
}
