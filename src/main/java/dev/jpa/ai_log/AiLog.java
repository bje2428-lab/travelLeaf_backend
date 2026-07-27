package dev.jpa.ai_log;

import dev.jpa.ai_request.AiRequest;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "AI_LOG")
@Getter
@Setter
public class AiLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ai_log_seq")
    @SequenceGenerator(
            name = "ai_log_seq",
            sequenceName = "AI_LOG_SEQ",
            allocationSize = 1
    )
    @Column(name = "LOG_ID")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REQUEST_ID", nullable = false)
    private AiRequest aiRequest;

    @Column(name = "STATUS", length = 20, nullable = false)
    private String status; // SUCCESS / FAIL

    @Column(name = "LATENCY_MS")
    private Long latencyMs;

    @Lob
    @Column(name = "ERROR_MESSAGE")
    private String errorMessage;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_AT")
    private Date createdAt = new Date();
}
