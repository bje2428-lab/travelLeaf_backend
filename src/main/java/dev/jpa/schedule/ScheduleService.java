package dev.jpa.schedule;

import dev.jpa.ai_weather.AiWeatherRepository;
import dev.jpa.location.City;
import dev.jpa.location.CityRepository;
import dev.jpa.location.Region;
import dev.jpa.location.RegionRepository;
import dev.jpa.schedule.member.ScheduleMemberService;
import dev.jpa.user.User;
import dev.jpa.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final CityRepository cityRepository;
    private final ScheduleMemberService memberService;
    private final AiWeatherRepository aiWeatherRepository;

    public ScheduleService(
            ScheduleRepository scheduleRepository,
            UserRepository userRepository,
            RegionRepository regionRepository,
            CityRepository cityRepository,
            ScheduleMemberService memberService,
            AiWeatherRepository aiWeatherRepository
    ) {
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
        this.regionRepository = regionRepository;
        this.cityRepository = cityRepository;
        this.memberService = memberService;
        this.aiWeatherRepository = aiWeatherRepository;
    }

    /* =========================
       C) 일정 생성
    ========================= */
    public ScheduleDTO.Response createSchedule(ScheduleDTO.CreateRequest req) {

        if (req.getUserNo() == null) {
            throw new IllegalArgumentException("userNo는 필수입니다.");
        }

        User user = userRepository.findById(req.getUserNo())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자(userNo)"));

        Long startRegionId = (req.getStartRegionId() != null) ? req.getStartRegionId() : req.getRegionId();
        Long startCityId   = (req.getStartCityId() != null) ? req.getStartCityId() : req.getCityId();
        Long endRegionId   = (req.getEndRegionId() != null) ? req.getEndRegionId() : startRegionId;
        Long endCityId     = (req.getEndCityId() != null) ? req.getEndCityId() : startCityId;

        Long regionId = (req.getRegionId() != null) ? req.getRegionId() : startRegionId;
        Long cityId   = (req.getCityId() != null) ? req.getCityId() : startCityId;

        Schedule s = new Schedule();
        s.setUser(user);

        if (regionId != null) {
            Region r = regionRepository.findById(regionId)
                    .orElseThrow(() -> new IllegalArgumentException("대표 지역이 존재하지 않습니다."));
            s.setRegion(r);
        }
        if (cityId != null) {
            City c = cityRepository.findById(cityId)
                    .orElseThrow(() -> new IllegalArgumentException("대표 도시가 존재하지 않습니다."));
            s.setCity(c);
        }

        if (startRegionId != null) {
            Region sr = regionRepository.findById(startRegionId)
                    .orElseThrow(() -> new IllegalArgumentException("출발 지역이 존재하지 않습니다."));
            s.setStartRegion(sr);
        }
        if (startCityId != null) {
            City sc = cityRepository.findById(startCityId)
                    .orElseThrow(() -> new IllegalArgumentException("출발 도시가 존재하지 않습니다."));
            s.setStartCity(sc);
        }
        if (endRegionId != null) {
            Region er = regionRepository.findById(endRegionId)
                    .orElseThrow(() -> new IllegalArgumentException("도착 지역이 존재하지 않습니다."));
            s.setEndRegion(er);
        }
        if (endCityId != null) {
            City ec = cityRepository.findById(endCityId)
                    .orElseThrow(() -> new IllegalArgumentException("도착 도시가 존재하지 않습니다."));
            s.setEndCity(ec);
        }

        s.setScheduleTitle(req.getScheduleTitle());
        s.setStartDate(req.getStartDate());
        s.setEndDate(req.getEndDate());
        s.setPeopleCount(req.getPeopleCount());
        s.setBudget(req.getBudget());
        s.setHashtags(req.getHashtags());
        s.setAiKeywords(req.getAiKeywords());
        s.setThumbnailImg(req.getThumbnailImg());
        s.setMemo(req.getMemo());
        s.setIsPublic(req.getIsPublic());
        s.setRequestDifficulty(req.getRequestDifficulty());

        s.setScheduleCode(generateCode("SCH"));
        if (s.getShareCode() == null || s.getShareCode().isBlank()) {
            s.setShareCode(generateCode("SHR"));
        }

        Schedule saved = scheduleRepository.save(s);

        // OWNER 멤버 자동 등록
        memberService.ensureOwnerMember(saved);

        return ScheduleDTO.Response.fromEntity(saved);
    }

    /* =========================
       U) 일정 수정
    ========================= */
    public ScheduleDTO.Response updateSchedule(Long scheduleId, ScheduleDTO.UpdateRequest req) {

        Schedule s = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일정(scheduleId)"));

        if (req.getRegionId() != null) {
            Region r = regionRepository.findById(req.getRegionId())
                    .orElseThrow(() -> new IllegalArgumentException("대표 지역이 존재하지 않습니다."));
            s.setRegion(r);
        }
        if (req.getCityId() != null) {
            City c = cityRepository.findById(req.getCityId())
                    .orElseThrow(() -> new IllegalArgumentException("대표 도시가 존재하지 않습니다."));
            s.setCity(c);
        }

        if (req.getScheduleTitle() != null) s.setScheduleTitle(req.getScheduleTitle());
        if (req.getStartDate() != null) s.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) s.setEndDate(req.getEndDate());
        if (req.getPeopleCount() != null) s.setPeopleCount(req.getPeopleCount());
        if (req.getBudget() != null) s.setBudget(req.getBudget());
        if (req.getHashtags() != null) s.setHashtags(req.getHashtags());
        if (req.getMemo() != null) s.setMemo(req.getMemo());
        if (req.getIsPublic() != null) s.setIsPublic(req.getIsPublic());
        if (req.getRequestDifficulty() != null) s.setRequestDifficulty(req.getRequestDifficulty());

        return ScheduleDTO.Response.fromEntity(scheduleRepository.save(s));
    }

    /* =========================
       R) 단건 조회 (권한 포함)
    ========================= */
    @Transactional(readOnly = true)
    public ScheduleDTO.Response getSchedule(Long scheduleId, Long userNo) {

        Schedule s = scheduleRepository.findByIdWithJoins(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다. scheduleId=" + scheduleId));

        if (userNo == null) {
            if (!"Y".equalsIgnoreCase(s.getIsPublic())) {
                throw new IllegalArgumentException("로그인이 필요합니다.");
            }
            return ScheduleDTO.Response.fromEntity(s);
        }

        Long ownerNo = (s.getUser() != null) ? s.getUser().getUserno() : null;
        boolean isOwner = ownerNo != null && ownerNo.equals(userNo);
        boolean isPublic = "Y".equalsIgnoreCase(s.getIsPublic());

        if (!isOwner && !isPublic) {
            memberService.assertCanView(scheduleId, userNo);
        }

        return ScheduleDTO.Response.fromEntity(s);
    }

    /* =========================
       R) 내 일정 목록
    ========================= */
    @Transactional(readOnly = true)
    public List<ScheduleDTO.Response> listMine(Long userNo) {

        if (userNo == null || userNo <= 0) {
            throw new IllegalArgumentException("userNo는 필수입니다.");
        }

        return scheduleRepository.findAllMineOrJoinedWithJoins(userNo)
                .stream()
                .map(ScheduleDTO.Response::fromEntity)
                .toList();
    }

    /* =========================
       R) 단건 조회 (엔티티 반환 - AI_WEATHER 전용)
    ========================= */
    @Transactional(readOnly = true)
    public Schedule getScheduleEntity(Long scheduleId, Long userNo) {

        Schedule s = scheduleRepository.findByIdWithJoins(scheduleId)
                .orElseThrow(() ->
                        new IllegalArgumentException("일정을 찾을 수 없습니다. scheduleId=" + scheduleId)
                );

        if (userNo == null) {
            if (!"Y".equalsIgnoreCase(s.getIsPublic())) {
                throw new IllegalArgumentException("로그인이 필요합니다.");
            }
            return s;
        }

        Long ownerNo = (s.getUser() != null) ? s.getUser().getUserno() : null;
        boolean isOwner = ownerNo != null && ownerNo.equals(userNo);
        boolean isPublic = "Y".equalsIgnoreCase(s.getIsPublic());

        if (!isOwner && !isPublic) {
            memberService.assertCanView(scheduleId, userNo);
        }

        return s;
    }

    /* =========================
       D) 삭제 (FK: AI_WEATHER → SCHEDULE)
    ========================= */
    public void deleteSchedule(Long scheduleId, Long userNo) {

        Schedule s = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일정(scheduleId)"));

        memberService.assertIsOwner(scheduleId, userNo);

        // FK 때문에 먼저 삭제
        aiWeatherRepository.deleteByScheduleId(scheduleId);

        scheduleRepository.delete(s);
    }

    /* =========================
       S) 공유 전용 조회
    ========================= */
    @Transactional(readOnly = true)
    public ScheduleDTO.Response getScheduleByShare(Long scheduleId) {

        Schedule s = scheduleRepository.findByIdWithJoins(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다. scheduleId=" + scheduleId));

        return ScheduleDTO.Response.fromEntity(s);
    }

    private String generateCode(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
