package dev.jpa.ai;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "SCHEDULE_AI_CACHE",
       uniqueConstraints = @UniqueConstraint(name = "UK_SAI_UNQ", columnNames = {"SCHEDULE_ID", "KIND"}))
@Getter @Setter
public class ScheduleAiCache {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SAI")
    @SequenceGenerator(name = "SEQ_SAI", sequenceName = "SEQ_SCHEDULE_AI_CACHE", allocationSize = 1)
    @Column(name = "AI_ID")
    private Long aiId;

    @Column(name = "SCHEDULE_ID", nullable = false)
    private Long scheduleId;

    @Column(name = "KIND", nullable = false, length = 30)
    private String kind;

    @Lob
    @Column(name = "CONTENT", nullable = false)
    private String content; // JSON 문자열 그대로 저장

    @Column(name = "MODEL", length = 50)
    private String model;

    @Column(name = "PROMPT_VERSION", length = 20)
    private String promptVersion;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
