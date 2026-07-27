package dev.jpa.quiz;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final QuizRepository quizRepo;

    @GetMapping("/day/{dayNo}")
    public List<QuizResponseDTO> quizByDay(@PathVariable("dayNo") int dayNo) {
        return quizService.getQuizByDay(dayNo);
    }

    @PostMapping("/solve")
    public ResponseEntity<Boolean> solveQuiz(
            @RequestParam("userId") Long userId,
            @RequestBody QuizSolveRequestDTO dto
    ) {
        return ResponseEntity.ok(quizService.solveQuiz(userId, dto));
    }

    // ✅ quizId optional 추가 (프론트 폴링 정확도 ↑)
    @GetMapping("/attempt/latest")
    public ResponseEntity<QuizAttempt> latestAttempt(
            @RequestParam("userId") Long userId,
            @RequestParam("dayNo") int dayNo,
            @RequestParam(value = "quizId", required = false) Long quizId
    ) {
        return ResponseEntity.ok(quizService.getLatestAttempt(userId, dayNo, quizId));
    }

    @GetMapping("/byIds")
    public ResponseEntity<List<QuizResponseDTO>> quizByIdsDto(@RequestParam("ids") String ids) {
        return ResponseEntity.ok(quizService.getQuizByIds(ids));
    }

    @GetMapping("/ids")
    public ResponseEntity<List<Quiz>> quizByIdsEntity(@RequestParam("ids") String ids) {
        List<Long> idList = java.util.Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(Long::valueOf)
                .toList();

        return ResponseEntity.ok(quizRepo.findAllById(idList));
    }

    @PostMapping("/admin")
    public ResponseEntity<Void> createQuiz(@RequestBody QuizDTO dto) {
        quizService.createQuiz(dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/list")
    public List<QuizAdminListDTO> getAdminQuizList() {
        return quizService.getAdminQuizList();
    }

    @PutMapping("/admin/{quizId}")
    public ResponseEntity<Void> updateQuiz(
            @PathVariable("quizId") Long quizId,
            @RequestBody Map<String, String> body
    ) {
        quizService.updateQuestion(quizId, body.get("question"));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/admin/{quizId}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable("quizId") Long quizId) {
        quizService.deleteById(quizId);
        return ResponseEntity.ok().build();
    }
}
