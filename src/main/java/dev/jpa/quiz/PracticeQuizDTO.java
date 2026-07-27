package dev.jpa.quiz;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PracticeQuizDTO {
    private String question;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private int correctNo;
    private String explanation;
}
