package dev.jpa.ai.user_profile_embedding;

import java.io.StringReader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserProfileEmbeddingRepository {

    private final DataSource dataSource;

    public void saveOrUpdate(String userId, String embeddingJson) {

        try (Connection conn = dataSource.getConnection()) {

            // 1️⃣ 존재 여부
            boolean exists;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM USER_PROFILE_EMBEDDINGS WHERE USER_ID = ?")) {
                ps.setString(1, userId);
                ResultSet rs = ps.executeQuery();
                rs.next();
                exists = rs.getInt(1) > 0;
            }

            if (exists) {
                // 2️⃣ UPDATE
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE USER_PROFILE_EMBEDDINGS SET EMBEDDING = ?, UPDATED_AT = SYSDATE WHERE USER_ID = ?")) {
                    ps.setCharacterStream(1, new StringReader(embeddingJson), embeddingJson.length());
                    ps.setString(2, userId);
                    ps.executeUpdate();
                }
            } else {
                // 3️⃣ INSERT
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO USER_PROFILE_EMBEDDINGS (USER_ID, EMBEDDING, UPDATED_AT) VALUES (?, ?, SYSDATE)")) {
                    ps.setString(1, userId);
                    ps.setCharacterStream(2, new StringReader(embeddingJson), embeddingJson.length());
                    ps.executeUpdate();
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("CLOB save failed", e);
        }
    }

    public String findEmbedding(String userId) {

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT EMBEDDING FROM USER_PROFILE_EMBEDDINGS WHERE USER_ID = ?")) {

            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;

            Clob clob = rs.getClob(1);
            return clob.getSubString(1, (int) clob.length());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
