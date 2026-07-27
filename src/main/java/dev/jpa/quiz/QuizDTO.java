package dev.jpa.quiz;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizDTO {

    private String category;
    private String question;

    private String option1;
    private String option2;
    private String option3;
    private String option4;

    private int correctNo;
    private String explanation;
    private int expReward;
    
    private int dayNo;        // 몇 일차
    private int sortOrder;    // 그 날의 몇 번째 문제 (1~4)
}
