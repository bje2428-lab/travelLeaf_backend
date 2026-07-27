package dev.jpa.quiz;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizAdminListDTO {

    private Long quizId;
    private int dayNo;
    private int sortOrder;
    private String category;
    private String question;
}
