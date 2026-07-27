package dev.jpa.notice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    // ===============================
    // 관리자 권한 체크
    // ===============================
    private void checkAdmin(int grade) {
        if (grade != 2) {
            throw new RuntimeException("관리자만 수행할 수 있습니다.");
        }
    }

    // =====================================================
    // 📌 1) 공지 등록 (JSON)
    // =====================================================
    @PostMapping(consumes = "application/json")
    public Notice create(@RequestBody NoticeDTO dto) {
        checkAdmin(dto.getGrade());
        return noticeService.create(dto);
    }

    // =====================================================
    // 📌 1-2) 공지 등록 (파일 포함)
    // =====================================================
    @PostMapping(consumes = "multipart/form-data")
    public Notice createWithFile(
            @ModelAttribute NoticeDTO dto,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        checkAdmin(dto.getGrade());
        return noticeService.create(dto, file);
    }

    // =====================================================
    // 📌 2) 공지 상세조회
    // =====================================================
    @GetMapping("/{id}")
    public Notice get(
            @PathVariable(name = "id") Long id
    ) {
        return noticeService.get(id);
    }

    // =====================================================
    // 📌 3) 공지 목록 조회 (페이징)
    //  ⭐ 상단 고정 + 최신순 적용
    // =====================================================
    @GetMapping
    public Page<Notice> list(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by("isFixed").descending()
                                .and(Sort.by("noticeId").descending())
                );

        return noticeService.list(pageable);
    }

    // =====================================================
    // 📌 4) 공지 수정 (파일 포함)
    // =====================================================
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public Notice update(
            @PathVariable(name = "id") Long id,
            @ModelAttribute NoticeDTO dto,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        checkAdmin(dto.getGrade());
        return noticeService.update(id, dto, file);
    }

    // =====================================================
    // 📌 5) 공지 삭제
    // =====================================================
    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "grade") Integer grade
    ) {
        checkAdmin(grade);
        noticeService.delete(id);
    }

    // =====================================================
    // 📌 6) 공지 검색 (페이징)
    //  ⭐ 상단 고정 + 최신순 적용
    // =====================================================
    @GetMapping("/search")
    public Page<Notice> search(
            @RequestParam(name = "keyword") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by("isFixed").descending()
                                .and(Sort.by("noticeId").descending())
                );

        return noticeService.search(keyword, pageable);
    }

    // =====================================================
    // 📌 7) 카테고리별 조회 (페이징)
    //  ⭐ 상단 고정 + 최신순 적용
    // =====================================================
    @GetMapping("/category/{category}")
    public Page<Notice> category(
            @PathVariable(name = "category") String category,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by("isFixed").descending()
                                .and(Sort.by("noticeId").descending())
                );

        return noticeService.byCategory(category, pageable);
    }
}
