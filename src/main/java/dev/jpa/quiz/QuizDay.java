package dev.jpa.quiz;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "QUIZ_DAY")
@Getter @Setter
public class QuizDay {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "quiz_day_seq")
    @SequenceGenerator(name = "quiz_day_seq", sequenceName = "QUIZ_DAY_SEQ", allocationSize = 1)
    @Column(name = "QUIZ_DAY_ID")
    private Long quizDayId;

    @Column(name = "DAY_NO")
    private int dayNo;

    @Column(name = "QUIZ_ID")
    private Long quizId;

    @Column(name = "SORT_ORDER")
    private int sortOrder;

    @Column(name = "ACTIVE_YN")
    private String activeYn;
    
    
}
