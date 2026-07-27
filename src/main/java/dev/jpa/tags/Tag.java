package dev.jpa.tags;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TAGS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tag_seq_gen")
    @SequenceGenerator(
        name = "tag_seq_gen",
        sequenceName = "TAGS_SEQ",   // DB 시퀀스 이름과 반드시 동일
        allocationSize = 1
    )
    @Column(name = "tag_id")
    private Long tagId;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Builder.Default
    @Column(name = "use_count")
    private Integer useCount = 0;   // 태그 사용 횟수 (기본 0)

}
