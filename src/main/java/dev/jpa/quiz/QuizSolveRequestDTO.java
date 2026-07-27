package dev.jpa.quiz;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizSolveRequestDTO {

    private Long quizId;
    private int selectedNo;
    private int dayNo;
}
