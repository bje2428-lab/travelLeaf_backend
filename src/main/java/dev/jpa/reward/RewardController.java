package dev.jpa.reward;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reward")
@RequiredArgsConstructor
public class RewardController {

    private final RewardService rewardService;
    private final UserStatusRepository userStatusRepo;

    /* =====================
       보상 지급
    ===================== */
    @PostMapping("/grant")
    public RewardResponse grantReward(
            @RequestBody RewardGrantRequest request) {
        return rewardService.grantReward(request);
    }

    /* =====================
       ⭐ 내 성장 상태 조회
       GET /reward/status?userNo=1
    ===================== */
    @GetMapping("/status")
    public Map<String, Object> getStatus(
            @RequestParam("userNo") Long userNo
    ) {

        UserStatus s = userStatusRepo.findById(userNo)
                .orElseGet(() -> {
                    UserStatus ns = new UserStatus();
                    ns.setUserNo(userNo);
                    return userStatusRepo.save(ns);
                });

        int nextLevelExp = s.getCurrentLevel() * 100;

        Map<String, Object> map = new HashMap<>();
        map.put("currentLevel", s.getCurrentLevel());
        map.put("currentExp", s.getCurrentExp());
        map.put("nextLevelExp", nextLevelExp);
        map.put("prevLevel", s.getCurrentLevel() - 1);

        return map;
    }

    /* =====================
       ✅ 추가 1) 보상 정의 목록 (REWARD_MASTER)
       GET /reward/master
    ===================== */
    @GetMapping("/master")
    public List<RewardMaster> getRewardMasters() {
        return rewardService.getRewardMasters();
    }

    /* =====================
       ✅ 추가 2) 보상 지급 기록 (REWARD_LOG)
       GET /reward/log?userNo=1
    ===================== */
    @GetMapping("/log")
    public List<RewardLog> getRewardLogs(
            @RequestParam("userNo") Long userNo
    ) {
        return rewardService.getRewardLogs(userNo);
    }
}
