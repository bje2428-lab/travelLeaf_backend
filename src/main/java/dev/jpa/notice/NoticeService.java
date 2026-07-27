package dev.jpa.notice;

import dev.jpa.tool.Tool; // ✨ Tool 클래스 임포트 필수!

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    // =====================================================
    // 공지 등록 (JSON 전용, 파일 없음)
    // =====================================================
    public Notice create(NoticeDTO dto) {
        Notice notice = Notice.builder()
                .userId(dto.getUserId())
                .isFixed(dto.getIsFixed())
                .title(dto.getTitle())
                .content(dto.getContent())
                .fileUrl(dto.getFileUrl())
                .category(dto.getCategory())
                .createdAt(new Date())
                .viewCount(0L)
                .build();

        return noticeRepository.save(notice);
    }

    // =====================================================
    // 공지 등록 (파일 첨부)
    // =====================================================
    public Notice create(NoticeDTO dto, MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            try {
                String uploadDir = Tool.getServerDir("notice");
                Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
                Files.createDirectories(uploadPath);

                String fileName =
                        UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);

                Files.copy(file.getInputStream(), filePath);

                String fileUrl = "/notice/storage/" + fileName;
                dto.setFileUrl(fileUrl);
            } catch (IOException e) {
                throw new RuntimeException("파일 업로드에 실패했습니다.", e);
            }
        }
        return create(dto);
    }

    // =====================================================
    // 상세조회 + 조회수 증가
    // =====================================================
    public Notice get(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공지 없음"));

        notice.setViewCount(notice.getViewCount() + 1);
        return noticeRepository.save(notice);
    }

    // =====================================================
    // 공지 수정 (JSON 전용)
    // =====================================================
    public Notice update(Long id, NoticeDTO dto) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공지 없음"));

        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setIsFixed(dto.getIsFixed());
        notice.setFileUrl(dto.getFileUrl());
        notice.setCategory(dto.getCategory());
        notice.setUpdatedAt(new Date());

        return noticeRepository.save(notice);
    }

    // =====================================================
    // 공지 수정 (파일 첨부)
    // =====================================================
    public Notice update(Long id, NoticeDTO dto, MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            try {
                String uploadDir = Tool.getServerDir("notice");
                Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
                Files.createDirectories(uploadPath);

                String fileName =
                        UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);

                Files.copy(file.getInputStream(), filePath);

                String fileUrl = "/notice/storage/" + fileName;
                dto.setFileUrl(fileUrl);
            } catch (IOException e) {
                throw new RuntimeException("파일 업로드에 실패했습니다.", e);
            }
        }
        return update(id, dto);
    }

    // =====================================================
    // 삭제
    // =====================================================
    public void delete(Long id) {
        noticeRepository.deleteById(id);
    }

    // =====================================================
    // ❌ 기존 전체 목록 (페이징 없음) — 유지
    // =====================================================
    public List<Notice> list() {
        return noticeRepository.findAll()
                .stream()
                .sorted((a, b) -> {
                    if (!a.getIsFixed().equals(b.getIsFixed())) {
                        return b.getIsFixed().compareTo(a.getIsFixed());
                    }
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .toList();
    }

    // =====================================================
    // ✅ 페이징 목록 조회 (🔥 새로 추가 🔥)
    // =====================================================
    public Page<Notice> list(Pageable pageable) {
        return noticeRepository.findAll(pageable);
    }

    // =====================================================
    // ❌ 기존 검색 (페이징 없음) — 유지
    // =====================================================
    public List<Notice> search(String keyword) {
        return noticeRepository
                .findByTitleContainingOrContentContaining(keyword, keyword);
    }

    // =====================================================
    // ❌ 기존 카테고리 조회 (페이징 없음) — 유지
    // =====================================================
    public List<Notice> byCategory(String category) {
        return noticeRepository
                .findByCategoryOrderByCreatedAtDesc(category);
    }

    // =====================================================
    // ✅ (선택) 검색 + 페이징
    // =====================================================
    public Page<Notice> search(String keyword, Pageable pageable) {
        return noticeRepository
                .findByTitleContainingOrContentContaining(keyword, keyword, pageable);
    }

    // =====================================================
    // ✅ (선택) 카테고리 + 페이징
    // =====================================================
    public Page<Notice> byCategory(String category, Pageable pageable) {
        return noticeRepository.findByCategory(category, pageable);
    }
}
