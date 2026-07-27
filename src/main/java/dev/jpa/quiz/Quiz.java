package dev.jpa.quiz;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(name = "QUIZ")
@Getter
@Setter
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "quiz_seq")
    @SequenceGenerator(
        name = "quiz_seq",
        sequenceName = "QUIZ_SEQ",
        allocationSize = 1
    )
    @Column(name = "QUIZ_ID")
    private Long quizId;

    private String category;
    private String question;

    // 🔥🔥🔥 이게 핵심
    @Column(name = "OPTION_1")
    private String option1;

    @Column(name = "OPTION_2")
    private String option2;

    @Column(name = "OPTION_3")
    private String option3;

    @Column(name = "OPTION_4")
    private String option4;

    @Column(name = "CORRECT_NO")
    private int correctNo;

    private String explanation;

    @Column(name = "EXP_REWARD")
    private int expReward;

    @Temporal(TemporalType.DATE)
    private Date createdAt;
    
    @Column(name = "GENERATED_YN")
    private String generatedYn;  // 'Y' or 'N'

    @Column(name = "BASE_QUIZ_ID")
    private Long baseQuizId;

}
