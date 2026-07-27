package dev.jpa.ai_weather;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dev.jpa.ai_weather_core.RiskLevel;
import dev.jpa.schedule.ScheduleService;

@RestController
@RequestMapping("/ai_weather")
public class AiWeatherCont {

    private final AiWeatherService aiWeatherService;
    private final AiWeatherRepository aiWeatherRepository;
    private final ScheduleService scheduleService;

    public AiWeatherCont(
            AiWeatherService aiWeatherService,
            AiWeatherRepository aiWeatherRepository,
            ScheduleService scheduleService
    ) {
        this.aiWeatherService = aiWeatherService;
        this.aiWeatherRepository = aiWeatherRepository;
        this.scheduleService = scheduleService;
    }

    /**
     * ✅ 일정 날씨 분석 생성
     */
    @PostMapping("/{scheduleId}/summary")
    public ResponseEntity<?> createWeatherAnalysis(
            @PathVariable("scheduleId") Long scheduleId,
            @RequestParam("userNo") Long userNo
    ) {
        try {
            aiWeatherService.createWeatherAnalysis(scheduleId, userNo);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * ✅ 일정별 요약 날씨 조회 (Day 단위)
     */
    @GetMapping("/{scheduleId}")
    public ResponseEntity<AiWeatherDTO.Response> getWeatherAnalysis(
            @PathVariable("scheduleId") Long scheduleId,
            @RequestParam("userNo") Long userNo
    ) {
        try {
            scheduleService.getScheduleEntity(scheduleId, userNo);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }

        List<AiWeather> list =
                aiWeatherRepository.findLatestByScheduleId(scheduleId);

        Map<LocalDate, List<AiWeather>> byDate =
                list.stream()
                    .collect(Collectors.groupingBy(AiWeather::getTargetDate));

        List<AiWeatherDTO.DailyWeather> days =
                byDate.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> {

                        List<AiWeather> rows = entry.getValue();

                        RiskLevel level =
                                rows.stream()
                                    .map(r -> RiskLevel.valueOf(r.getRiskLevel()))
                                    .max(Enum::compareTo)
                                    .orElse(RiskLevel.SAFE);

                        AiWeather representative =
                                rows.stream()
                                    .filter(r -> "STAY".equals(r.getWeatherType()))
                                    .findFirst()
                                    .orElse(rows.get(0));

                        return new AiWeatherDTO.DailyWeather(
                                entry.getKey(),
                                level,
                                representative.getRiskReason(),
                                representative.getAiMessage(),
                                representative.getCreatedAt()
                        );
                    })
                    .toList();

        return ResponseEntity.ok(
                new AiWeatherDTO.Response(scheduleId, days)
        );
    }

    /**
     * ✅ detail 단건 날씨 조회 (지도 / 지점 클릭용)
     */
    @GetMapping("/detail/{detailId}")
    public ResponseEntity<List<AiWeather>> getWeatherByDetail(
            @PathVariable("detailId") Long detailId
    ) {
        List<AiWeather> list =
                aiWeatherRepository.findLatestByDetailId(detailId);

        return ResponseEntity.ok(list);
    }

    /**
     * ✅ 일정 + detail 전체 날씨 조회 (타임라인 / 지도 전체용)
     */
    @GetMapping("/schedule/{scheduleId}/details")
    public ResponseEntity<List<AiWeather>> getWeatherByScheduleDetails(
            @PathVariable("scheduleId") Long scheduleId,
            @RequestParam("userNo") Long userNo
    ) {
        try {
            scheduleService.getScheduleEntity(scheduleId, userNo);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }

        List<AiWeather> list =
                aiWeatherRepository.findLatestByScheduleIdOrderByDetail(scheduleId);

        return ResponseEntity.ok(list);
    }
    
    /**
     * ✅ DAY + CITY 기준 동선 날씨 조회 (핵심 API)
     * - 대표 지역 없음
     * - 동선에 등장한 모든 시군구 각각 설명
     */
    @GetMapping("/schedule/{scheduleId}/route")
    public ResponseEntity<AiWeatherRouteDTO.Response> getRouteWeather(
            @PathVariable("scheduleId") Long scheduleId,
            @RequestParam("userNo") Long userNo
    ) {
        return ResponseEntity.ok(
                aiWeatherService.getLatestRouteWeather(scheduleId, userNo)
        );
    }    
}
