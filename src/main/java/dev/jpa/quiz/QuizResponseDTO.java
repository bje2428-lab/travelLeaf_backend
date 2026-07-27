package dev.jpa.quiz;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizResponseDTO {

    private Long quizId;

    private String category;
    private String question;

    // 보기 (🔥 필수)
    private String option1;
    private String option2;
    private String option3;
    private String option4;

    // 정답 번호 (1~4)
    private int correctNo;

    // 해설
    private String explanation;

    // 경험치
    private int expReward;
}
