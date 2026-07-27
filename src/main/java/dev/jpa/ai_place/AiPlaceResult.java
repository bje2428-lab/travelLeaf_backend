package dev.jpa.ai_place;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "AIPLACE_RESULT")
@Getter
@Setter
public class AiPlaceResult {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AIPLACE_RESULT_SEQ")
    @SequenceGenerator(
        name = "AIPLACE_RESULT_SEQ",
        sequenceName = "AIPLACE_RESULT_SEQ",
        allocationSize = 1
    )
    private Long resultId;

    private String placeName;

    @Column(length = 1000)
    private String description;

    private Double confidence;

    private String sourceApi;

    @Temporal(TemporalType.DATE)
    private Date createdAt;
}
