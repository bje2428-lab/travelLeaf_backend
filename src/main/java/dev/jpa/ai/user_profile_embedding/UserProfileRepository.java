package dev.jpa.ai.user_profile_embedding;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserProfileRepository {

    private final JdbcTemplate jdbcTemplate;

    public String getUserProfileText(String userId) {

        String sql = """
        SELECT
            DBMS_LOB.SUBSTR(
                XMLAGG(
                    XMLELEMENT(e,
                        p.title || ' ' ||
                        DBMS_LOB.SUBSTR(p.content, 4000, 1) || ' ' ||
                        NVL(t.tags, '') || ' '
                    )
                    ORDER BY p.post_id
                ).getClobVal(),
                32767, 1
            ) AS profile_text
        FROM POSTS p
        LEFT JOIN (
            SELECT
                pt.post_id,
                LISTAGG(t.name, ' ') WITHIN GROUP (ORDER BY t.name) AS tags
            FROM POSTS_TAGS pt
            JOIN TAGS t ON pt.tag_id = t.tag_id
            GROUP BY pt.post_id
        ) t ON p.post_id = t.post_id
        WHERE p.post_id IN (
            SELECT DISTINCT l.post_id
            FROM USER_ACTIVITY_LOG l
            WHERE l.user_id = ?
              AND l.action IN ('VIEW','LIKE','COMMENT')
        )
        """;

        return jdbcTemplate.queryForObject(sql, String.class, userId);
    }
}
