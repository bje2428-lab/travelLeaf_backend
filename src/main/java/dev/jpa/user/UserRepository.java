package dev.jpa.user;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 아이디 중복 체크
    int countByUserid(String userid);

    // 로그인용
    int countByUseridAndPassword(String userid, String password);

    // 아이디로 유저 조회
    Optional<User> findByUserid(String userid);
    
   

    // 닉네임 중복 체크
    Optional<User> findByNickname(String nickname);

    // ⭐ 아이디 찾기 (이름 + 이메일)
    Optional<User> findByNameAndEmail(String name, String email);
    
    // 비밀번호 찾기
    Optional<User> findByUseridAndNameAndEmailAndPhone(
        String userid,
        String name,
        String email,
        String phone
    );
    
    // 🔥 검색 + 페이징
    Page<User> findByUseridContainingOrNameContainingOrEmailContainingOrNicknameContaining(
        String userid,
        String name,
        String email,
        String nickname,
        Pageable pageable
    );
    


    // 비밀번호 변경
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.password = :newPassword WHERE u.userid = :userid")
    int updatePassword(
        @Param("userid") String userid,
        @Param("newPassword") String newPassword
    );
    
    // 회원 탈퇴
    @Modifying
    @Transactional
    @Query(value = """
        UPDATE USER_TB
        SET
            STATUS = 'DELETE',
            USER_ID = 'DELETED_' || USER_NO,
            EMAIL = 'DELETED_' || USER_NO,
            PHONE = 'DELETED_' || USER_NO,
            NICKNAME = 'DELETED_' || USER_NO
        WHERE USER_ID = :userid
    """, nativeQuery = true)
    void withdraw(@Param("userid") String userid);

    

}
