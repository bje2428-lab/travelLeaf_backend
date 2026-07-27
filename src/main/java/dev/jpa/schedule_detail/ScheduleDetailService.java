package dev.jpa.schedule_detail;

import dev.jpa.location.City;
import dev.jpa.location.CityRepository;
import dev.jpa.location.Region;
import dev.jpa.location.RegionRepository;
import dev.jpa.places.Places;
import dev.jpa.places.PlacesRepository;
import dev.jpa.schedule.Schedule;
import dev.jpa.schedule.ScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ScheduleDetailService {

    private final ScheduleDetailRepository detailRepository;
    private final ScheduleRepository scheduleRepository;
    private final PlacesRepository placesRepository;
    private final RegionRepository regionRepository;
    private final CityRepository cityRepository;

    public ScheduleDetailService(
            ScheduleDetailRepository detailRepository,
            ScheduleRepository scheduleRepository,
            PlacesRepository placesRepository,
            RegionRepository regionRepository,
            CityRepository cityRepository
    ) {
        this.detailRepository = detailRepository;
        this.scheduleRepository = scheduleRepository;
        this.placesRepository = placesRepository;
        this.regionRepository = regionRepository;
        this.cityRepository = cityRepository;
    }

    /* =========================
       공통: Schedule 필수 체크
    ========================= */
    private Schedule mustSchedule(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() ->
                        new IllegalArgumentException("일정을 찾을 수 없습니다. scheduleId=" + scheduleId)
                );
    }

    /* =========================
       공통: Region / City 변환
    ========================= */
    private void applyRegionCity(ScheduleDetail detail, ScheduleDetailDTO.CreateRequest req) {
        if (req.getRegion() == null || req.getCity() == null) return;

        Region region = regionRepository.findByRegionName(req.getRegion())
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 region: " + req.getRegion())
                );

        City city = cityRepository.findByCityNameAndRegion(req.getCity(), region)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "존재하지 않는 city: " + req.getCity() + " / region=" + req.getRegion()
                        )
                );

        detail.setRegionId(region.getRegionId());
        detail.setCityId(city.getCityId());
    }

    /* =========================
       공통: place upsert
    ========================= */
    private Places upsertPlace(ScheduleDetailDTO.CreateRequest req) {
        if (req.getPlaceId() == null) return null;

        return placesRepository.findById(req.getPlaceId())
                .orElseGet(() -> {
                    Places p = new Places();
                    p.setPlaceId(req.getPlaceId());
                    p.setName(req.getPlaceName() != null ? req.getPlaceName() : "");
                    p.setCategory(req.getCategory());
                    p.setAddress(req.getAddress());
                    p.setLat(req.getLat());
                    p.setLng(req.getLng());
                    p.setSourceType("KAKAO");
                    return placesRepository.save(p);
                });
    }

    /* =========================
       C: 상세 일정 1건 생성
    ========================= */
    public ScheduleDetailDTO.Response createDetail(ScheduleDetailDTO.CreateRequest req) {
        Schedule schedule = mustSchedule(req.getScheduleId());
        Places place = upsertPlace(req);

        ScheduleDetail detail = new ScheduleDetail();
        detail.setSchedule(schedule);
        detail.setDayNumber(req.getDayNumber());
        detail.setOrderInDay(req.getOrderInDay());
        detail.setStopType(req.getStopType());

        detail.setPlace(place);
        detail.setPlaceName(req.getPlaceName());

        detail.setStartTime(req.getStartTime());
        detail.setEndTime(req.getEndTime());
        detail.setCost(req.getCost());
        detail.setMemo(req.getMemo());
        detail.setDistanceKM(req.getDistanceKM());

        applyRegionCity(detail, req);

        detailRepository.save(detail);
        return ScheduleDetailDTO.fromEntity(detail);
    }

    /* =========================
       R: day 조회
    ========================= */
    @Transactional(readOnly = true)
    public List<ScheduleDetailDTO.Response> getDay(Long scheduleId, Integer dayNumber) {
        return detailRepository
                .findBySchedule_ScheduleIdAndDayNumberOrderByOrderInDayAsc(scheduleId, dayNumber)
                .stream()
                .map(ScheduleDetailDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /* =========================
       U: day 덮어쓰기
    ========================= */
    public List<ScheduleDetailDTO.Response> replaceDay(
            Long scheduleId,
            Integer dayNumber,
            List<ScheduleDetailDTO.CreateRequest> rows
    ) {
        Schedule schedule = mustSchedule(scheduleId);
        detailRepository.deleteByScheduleIdAndDayNumber(scheduleId, dayNumber);

        for (ScheduleDetailDTO.CreateRequest req : rows) {
            req.setScheduleId(scheduleId);
            req.setDayNumber(dayNumber);

            Places place = upsertPlace(req);

            ScheduleDetail detail = new ScheduleDetail();
            detail.setSchedule(schedule);
            detail.setDayNumber(dayNumber);
            detail.setOrderInDay(req.getOrderInDay());
            detail.setStopType(req.getStopType());

            detail.setPlace(place);
            detail.setPlaceName(req.getPlaceName());

            detail.setStartTime(req.getStartTime());
            detail.setEndTime(req.getEndTime());
            detail.setCost(req.getCost());
            detail.setMemo(req.getMemo());
            detail.setDistanceKM(req.getDistanceKM());

            applyRegionCity(detail, req);

            detailRepository.save(detail);
        }

        return getDay(scheduleId, dayNumber);
    }

    /* =========================
       🔥 AI 일정 전체 저장
    ========================= */
    public void saveAiDetails(
            Long scheduleId,
            List<ScheduleDetailDTO.CreateRequest> requests
    ) {
        Schedule schedule = mustSchedule(scheduleId);

        requests.stream()
                .map(ScheduleDetailDTO.CreateRequest::getDayNumber)
                .distinct()
                .forEach(day ->
                        detailRepository.deleteByScheduleIdAndDayNumber(scheduleId, day)
                );

        for (ScheduleDetailDTO.CreateRequest req : requests) {
            Places place = upsertPlace(req);

            ScheduleDetail detail = new ScheduleDetail();
            detail.setSchedule(schedule);
            detail.setDayNumber(req.getDayNumber());
            detail.setOrderInDay(req.getOrderInDay());
            detail.setStopType(req.getStopType());

            detail.setPlace(place);
            detail.setPlaceName(req.getPlaceName());

            detail.setStartTime(req.getStartTime());
            detail.setEndTime(req.getEndTime());
            detail.setCost(req.getCost());
            detail.setMemo(req.getMemo());
            detail.setDistanceKM(req.getDistanceKM());

            applyRegionCity(detail, req);

            detailRepository.save(detail);
        }
    }

    /* =========================
       R: 일정 전체 조회
    ========================= */
    @Transactional(readOnly = true)
    public List<ScheduleDetailDTO.Response> getDetailsBySchedule(Long scheduleId) {
        return detailRepository
                .findBySchedule_ScheduleIdOrderByDayNumberAscOrderInDayAsc(scheduleId)
                .stream()
                .map(ScheduleDetailDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /* =========================
       R: 상세 단건 조회
    ========================= */
    @Transactional(readOnly = true)
    public ScheduleDetailDTO.Response getDetail(Long detailId) {
        ScheduleDetail detail = detailRepository.findById(detailId)
                .orElseThrow(() ->
                        new IllegalArgumentException("상세 일정을 찾을 수 없습니다. detailId=" + detailId)
                );
        return ScheduleDetailDTO.fromEntity(detail);
    }

    /* =========================
       D: 상세 삭제
    ========================= */
    public void deleteDetail(Long detailId) {
        ScheduleDetail detail = detailRepository.findById(detailId)
                .orElseThrow(() ->
                        new IllegalArgumentException("삭제할 상세 일정을 찾을 수 없습니다. detailId=" + detailId)
                );
        detailRepository.delete(detail);
    }
}
