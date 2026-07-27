package dev.jpa.friends;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FriendsRepository extends JpaRepository<Friends, Long> {

    /** 친구 관계 중복 체크 */
    @Query("""
        SELECT f FROM Friends f
        WHERE 
        (f.requesterId = :u1 AND f.receiverId = :u2)
        OR
        (f.requesterId = :u2 AND f.receiverId = :u1)
    """)
    Optional<Friends> findFriendRelation(
        @Param("u1") String u1,
        @Param("u2") String u2
    );

    /** 내가 보낸 요청 */
    List<Friends> findByRequesterIdAndSw(String requesterId, Integer sw);

    /** 내가 받은 요청 */
    List<Friends> findByReceiverIdAndSw(String receiverId, Integer sw);

    /** 친구 목록 */
    @Query("""
        SELECT f FROM Friends f
        WHERE 
        (f.requesterId = :userId OR f.receiverId = :userId)
        AND f.sw = 1
    """)
    List<Friends> findMyFriends(@Param("userId") String userId);
    
    // ==================================================
    // 🔥 채팅용: friendId → userNo 두 개 조회
    // ==================================================

    /**
     * 채팅용
     * friendId로 requesterNo / receiverNo 조회
     */
    @Query("""
        SELECT f FROM Friends f
        WHERE f.friendId = :friendId
    """)
    Optional<Friends> findForChat(@Param("friendId") Long friendId);
    
}
