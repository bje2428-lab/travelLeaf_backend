package dev.jpa.loginhistory;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    // 특정 유저 번호로 로그인 기록 조회
    Page<LoginHistory> findByUsernoOrderByLoginAtDesc(Long userno, Pageable pageable);

    // 검색: userid, nickname, 이름(name) 포함 조회
    @Query("""
        SELECT lh 
        FROM LoginHistory lh
        JOIN lh.user u
        WHERE (:keyword IS NULL 
               OR u.userid LIKE %:keyword% 
               OR u.nickname LIKE %:keyword%
               OR u.name LIKE %:keyword%)
        ORDER BY lh.loginAt DESC
        """)
    Page<LoginHistory> searchLoginHistory(@Param("keyword") String keyword, Pageable pageable);
    
    List<LoginHistory> findTop10ByUsernoOrderByLoginAtDesc(Long userno);
}
