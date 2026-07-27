package dev.jpa.reward;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RewardService {

    private final RewardLogRepository rewardLogRepo;
    private final RewardMasterRepository rewardMasterRepo;
    private final UserStatusRepository userStatusRepo;

    // ✅ 레벨업 메일
    private final LevelUpMailService levelUpMailService;

    @Transactional
    public RewardResponse grantReward(RewardGrantRequest req) {

        // ✅ 0) 필수값 검증 (rewardId 없으면 ORA-01400 다시 터짐)
        validateRequest(req);

        /* =====================
           1️⃣ 중복 지급 방지
        ===================== */
        if (rewardLogRepo.existsByUserNoAndSourceTypeAndSourceKey(
                req.getUserNo(),
                req.getSourceType(),
                req.getSourceKey())) {
            throw new IllegalStateException("이미 지급된 보상입니다.");
        }

        /* =====================
           2️⃣ USER_STATUS 확보
        ===================== */
        UserStatus status = userStatusRepo.findById(req.getUserNo())
                .orElseGet(() -> {
                    UserStatus s = new UserStatus();
                    s.setUserNo(req.getUserNo());
                    s.setCurrentExp(0);
                    s.setCurrentLevel(1);
                    s.setCurrentPoint(0);
                    s.setLastNotifiedLevel(0);
                    s.setUpdatedAt(new Date());
                    return userStatusRepo.save(s);
                });

        /* =====================
           3️⃣ 보상 계산
        ===================== */
        int rewardValue = calculateReward(req);

        /* =====================
           4️⃣ 보상 로그 저장 (✅ rewardId 반드시 저장)
        ===================== */
        RewardLog log = new RewardLog();
        log.setUserNo(req.getUserNo());
        log.setRewardId(req.getRewardId());      // ✅ NOT NULL 컬럼
        log.setRewardValue(rewardValue);
        log.setSourceType(req.getSourceType());
        log.setSourceKey(req.getSourceKey());
        rewardLogRepo.save(log);

        /* =====================
           5️⃣ 경험치 반영
        ===================== */
        int prevLevel = status.getCurrentLevel();
        status.setCurrentExp(status.getCurrentExp() + rewardValue);

        /* =====================
           6️⃣ 레벨 계산 (100xp = 1레벨)
        ===================== */
        int newLevel = status.getCurrentExp() / 100 + 1;
        status.setCurrentLevel(newLevel);
        status.setUpdatedAt(new Date());

        /* =====================
           7️⃣ 레벨업 메일 + lastNotifiedLevel 처리
           - 레벨업 했을 때만
           - 동일 레벨 중복 발송 방지
           - ✅ 메일 실패해도 lastNotifiedLevel은 업데이트(중복방지 확실)
        ===================== */
        if (newLevel > prevLevel) {
            int lastNotified = (status.getLastNotifiedLevel() == null) ? 0 : status.getLastNotifiedLevel();

            if (lastNotified < newLevel) {

                // ✅ 이메일 있을 때만 발송 시도
                if (req.getEmail() != null && !req.getEmail().isBlank()) {
                    try {
                        levelUpMailService.sendLevelUpMail(req.getEmail(), newLevel);
                    } catch (Exception e) {
                        System.out.println("[LEVEL-UP MAIL] send failed: " + e.getMessage());
                    }
                }

                // ✅ 성공/실패와 무관하게 “이 레벨은 처리됨” 기록
                status.setLastNotifiedLevel(newLevel);
            }
        }

        userStatusRepo.save(status);

        return new RewardResponse(
                rewardValue,
                status.getCurrentExp(),
                status.getCurrentLevel(),
                prevLevel
        );
    }

    public List<RewardMaster> getRewardMasters() {
        return rewardMasterRepo.findAll();
    }

    public List<RewardLog> getRewardLogs(Long userNo) {
        return rewardLogRepo.findByUserNoOrderByCreatedAtDesc(userNo);
    }

    private int calculateReward(RewardGrantRequest req) {

        if ("QUIZ".equals(req.getSourceType())) {
            Integer correct = req.getCorrectCount();
            Integer total = req.getTotalCount();

            if (correct == null || total == null) return 0;

            if (correct.equals(total) && total == 4) return 50;

            return correct * 10;
        }

        if ("SURVEY".equals(req.getSourceType())) {
            Integer surveyReward = req.getSurveyReward();
            return surveyReward != null ? surveyReward : 0;
        }

        return 0;
    }

    private void validateRequest(RewardGrantRequest req) {
        if (req == null) throw new IllegalArgumentException("요청이 비어있습니다.");
        if (req.getUserNo() == null) throw new IllegalArgumentException("userNo가 필요합니다.");
        if (req.getSourceType() == null || req.getSourceType().isBlank())
            throw new IllegalArgumentException("sourceType이 필요합니다.");
        if (req.getSourceKey() == null || req.getSourceKey().isBlank())
            throw new IllegalArgumentException("sourceKey가 필요합니다.");

        // ✅ REWARD_LOG.REWARD_ID NOT NULL 대응
        if (req.getRewardId() == null)
            throw new IllegalArgumentException("rewardId가 필요합니다. (REWARD_LOG.REWARD_ID NOT NULL)");
    }
}
