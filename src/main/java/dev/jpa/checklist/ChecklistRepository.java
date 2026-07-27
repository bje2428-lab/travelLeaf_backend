package dev.jpa.checklist;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChecklistRepository extends JpaRepository<Checklist, Long> {

  /** 카테고리별 정렬 조회 */
  List<Checklist> findByCategoryOrderByItemIdAsc(String category);

  /** 전체 조회 (카테고리 → ITEM_ID) */
  List<Checklist> findAllByOrderByCategoryAscItemIdAsc();

  /**
   * 체크리스트 항목 수정  
   * (예시 MembersRepository 의 UPDATE 방식과 동일하게 nativeQuery 사용)
   */
  @Modifying
  @Query(
      value = "UPDATE CHECKLIST " +
              "SET CATEGORY = :category, " +
              "    ITEM_NAME = :itemName, " +
              "    DESCRIPTION = :description, " +
              "    CREATED_AT = :createdAt " +
              "WHERE ITEM_ID = :itemId",
      nativeQuery = true
  )
  int update(
          @Param("category") String category,
          @Param("itemName") String itemName,
          @Param("description") String description,
          @Param("createdAt") String createdAt,
          @Param("itemId") long itemId
  );
}
