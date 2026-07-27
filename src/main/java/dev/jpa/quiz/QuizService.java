package dev.jpa.quiz;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepo;
    private final QuizDayRepository quizDayRepo;
    private final QuizAttemptRepository attemptRepo;

    private final ApplicationEventPublisher publisher;

    public List<QuizResponseDTO> getQuizByDay(int dayNo) {
        return quizDayRepo.findQuizByDay(dayNo)
                .stream()
                .map(q -> {
                    QuizResponseDTO dto = new QuizResponseDTO();
                    dto.setQuizId(q.getQuizId());
                    dto.setCategory(q.getCategory());
                    dto.setQuestion(q.getQuestion());
                    dto.setOption1(q.getOption1());
                    dto.setOption2(q.getOption2());
                    dto.setOption3(q.getOption3());
                    dto.setOption4(q.getOption4());
                    dto.setCorrectNo(q.getCorrectNo());
                    dto.setExplanation(q.getExplanation());
                    dto.setExpReward(q.getExpReward());
                    return dto;
                })
                .toList();
    }

    @Transactional
    public boolean solveQuiz(Long userId, QuizSolveRequestDTO req) {

        Quiz quiz = quizRepo.findById(req.getQuizId())
                .orElseThrow(() -> new RuntimeException("퀴즈 없음"));

        boolean correct = quiz.getCorrectNo() == req.getSelectedNo();

        QuizAttempt attempt = new QuizAttempt();
        attempt.setUserId(userId);
        attempt.setQuizId(req.getQuizId());
        attempt.setDayNo(req.getDayNo());
        attempt.setIsCorrect(correct ? "Y" : "N");
        attempt.setAttemptedAt(new Date());

        // ✅ AI는 비동기 작업 전
        attempt.setAiStatus("PENDING");
        attempt.setAiUpdatedAt(new Date());

        attemptRepo.save(attempt);

        // ✅ 커밋 후 리스너가 잡아서 jobService.process 실행
        publisher.publishEvent(new QuizAttemptCreatedEvent(
                attempt.getAttemptId(),
                quiz.getQuizId(),
                req.getSelectedNo(),
                correct
        ));

        return correct;
    }

    // ✅ (수정) quizId가 있으면 그 attempt를 폴링해야 안전함
    public QuizAttempt getLatestAttempt(Long userId, int dayNo, Long quizId) {
        if (quizId != null) {
            return attemptRepo.findTopByUserIdAndDayNoAndQuizIdOrderByAttemptIdDesc(userId, dayNo, quizId)
                    .orElse(null);
        }
        return attemptRepo.findTopByUserIdAndDayNoOrderByAttemptedAtDescAttemptIdDesc(userId, dayNo)
                .orElse(null);
    }

    public List<QuizResponseDTO> getQuizByIds(String idsCsv) {

        if (idsCsv == null || idsCsv.isBlank()) return List.of();

        List<Long> ids = List.of(idsCsv.split(",")).stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(Long::valueOf)
                .toList();

        return quizRepo.findAllById(ids).stream()
                .filter(Objects::nonNull)
                .map(q -> {
                    QuizResponseDTO dto = new QuizResponseDTO();
                    dto.setQuizId(q.getQuizId());
                    dto.setCategory(q.getCategory());
                    dto.setQuestion(q.getQuestion());
                    dto.setOption1(q.getOption1());
                    dto.setOption2(q.getOption2());
                    dto.setOption3(q.getOption3());
                    dto.setOption4(q.getOption4());
                    dto.setCorrectNo(q.getCorrectNo());
                    dto.setExplanation(q.getExplanation());
                    dto.setExpReward(q.getExpReward());
                    return dto;
                })
                .toList();
    }

    @Transactional
    public void createQuiz(QuizDTO dto) {
        Quiz quiz = new Quiz();
        quiz.setCategory(dto.getCategory());
        quiz.setQuestion(dto.getQuestion());
        quiz.setOption1(dto.getOption1());
        quiz.setOption2(dto.getOption2());
        quiz.setOption3(dto.getOption3());
        quiz.setOption4(dto.getOption4());
        quiz.setCorrectNo(dto.getCorrectNo());
        quiz.setExplanation(dto.getExplanation());
        quiz.setExpReward(dto.getExpReward());
        quiz.setCreatedAt(new Date());
        quizRepo.save(quiz);

        QuizDay quizDay = new QuizDay();
        quizDay.setDayNo(dto.getDayNo());
        quizDay.setQuizId(quiz.getQuizId());
        quizDay.setSortOrder(dto.getSortOrder());
        quizDay.setActiveYn("Y");
        quizDayRepo.save(quizDay);
    }

    public List<QuizAdminListDTO> getAdminQuizList() {
        List<QuizDay> dayList = quizDayRepo.findAll();

        return dayList.stream()
                .map(d -> {
                    Quiz q = quizRepo.findById(d.getQuizId()).orElse(null);
                    if (q == null) return null;

                    QuizAdminListDTO dto = new QuizAdminListDTO();
                    dto.setQuizId(q.getQuizId());
                    dto.setDayNo(d.getDayNo());
                    dto.setSortOrder(d.getSortOrder());
                    dto.setCategory(q.getCategory());
                    dto.setQuestion(q.getQuestion());
                    return dto;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Transactional
    public void updateQuestion(Long quizId, String question) {
        Quiz quiz = quizRepo.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("퀴즈 없음"));
        quiz.setQuestion(question);
        quizRepo.save(quiz);
    }

    @Transactional
    public void deleteById(Long quizId) {
        quizDayRepo.deleteByQuizId(quizId);
        attemptRepo.deleteByQuizId(quizId);
        quizRepo.deleteById(quizId);
    }

    public boolean isTodayQuizCompleted(Long userId, int dayNo) {
        int totalQuizCount = quizDayRepo.countQuizByDay(dayNo);
        int solvedQuizCount = attemptRepo.countSolvedByDay(userId, dayNo);
        return solvedQuizCount >= totalQuizCount && totalQuizCount > 0;
    }
}
