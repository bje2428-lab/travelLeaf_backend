package dev.jpa.posts;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class PageResponse<T> {
  private List<T> content;  // 페이징된 목록
  private int page;            // 0-base
  private int size;
  private long totalElements;
  private int totalPages;
}

