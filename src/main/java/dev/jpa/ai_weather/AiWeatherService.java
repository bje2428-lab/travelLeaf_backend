package dev.jpa.ai_weather;

import dev.jpa.ai_log.AiLogService;
import dev.jpa.ai_request.AiRequest;
import dev.jpa.ai_request.AiRequestDTO;
import dev.jpa.ai_request.AiRequestService;
import dev.jpa.ai_weather_core.*;
import dev.jpa.location.City;
import dev.jpa.location.Region;
import dev.jpa.places.Places;
import dev.jpa.schedule.Schedule;
import dev.jpa.schedule.ScheduleService;
import dev.jpa.schedule_detail.ScheduleDetail;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
public class AiWeatherService {

    private final AiWeatherRepository aiWeatherRepository;
    private final ScheduleService scheduleService;
    private final AiRequestService aiRequestService;
    private final AiLogService aiLogService;
    private final WeatherProvider weatherProvider;
    private final AirQualityProvider airQualityProvider;
    private final AiWeatherAiService aiWeatherAiService;

    @PersistenceContext
    private EntityManager em;

    public AiWeatherService(
            AiWeatherRepository aiWeatherRepository,
            ScheduleService scheduleService,
            AiRequestService aiRequestService,
            AiLogService aiLogService,
            WeatherProvider weatherProvider,
            AirQualityProvider airQualityProvider,
            AiWeatherAiService aiWeatherAiService
    ) {
        this.aiWeatherRepository = aiWeatherRepository;
        this.scheduleService = scheduleService;
        this.aiRequestService = aiRequestService;
        this.aiLogService = aiLogService;
        this.weatherProvider = weatherProvider;
        this.airQualityProvider = airQualityProvider;
        this.aiWeatherAiService = aiWeatherAiService;
    }

    @Transactional
    public void createWeatherAnalysis(Long scheduleId, Long userNo) {

        long startTime = System.currentTimeMillis();

        // =========================
        // 1️⃣ AI_REQUEST 생성
        // =========================
        AiRequestDTO reqDto = new AiRequestDTO();
        reqDto.setUserNo(userNo);
        reqDto.setAiType("WEATHER");
        reqDto.setInputSummary("일정 날씨+대기질 분석: scheduleId=" + scheduleId);

        AiRequest aiRequest = aiRequestService.save(reqDto);

        try {
            // =========================
            // 2️⃣ 일정 조회
            // =========================
            Schedule schedule = scheduleService.getScheduleEntity(scheduleId, userNo);

            if (schedule.getStartDate() == null || schedule.getEndDate() == null) {
                throw new IllegalStateException("일정에 시작일 또는 종료일이 없습니다.");
            }

            // =========================
            // 3️⃣ 일정 상세 전체 조회
            // =========================
            List<ScheduleDetail> allDetails = em.createQuery("""
                    select d
                    from ScheduleDetail d
                    where d.schedule.scheduleId = :scheduleId
                    order by d.dayNumber asc, d.orderInDay asc
                    """, ScheduleDetail.class)
                    .setParameter("scheduleId", scheduleId)
                    .getResultList();

            // =========================
            // 4️⃣ 날짜별 분석
            // =========================
            Region representativeRegion = getRepresentativeRegion(schedule);
            if (representativeRegion == null) {
                throw new IllegalStateException("일정에 지역 정보가 없어 날씨 분석을 할 수 없습니다.");
            }

            LocalDate date = schedule.getStartDate();
            while (!date.isAfter(schedule.getEndDate())) {

                int dayNumber = calcDayNumber(schedule.getStartDate(), date);

                // 해당 날짜(dayNumber)의 detail들
                List<ScheduleDetail> dayDetails = new ArrayList<>();
                for (ScheduleDetail d : allDetails) {
                    if (d.getDayNumber() != null && d.getDayNumber() == dayNumber) {
                        dayDetails.add(d);
                    }
                }

                // dayDetails가 없으면 fallback 1건
                if (dayDetails.isEmpty()) {
                    saveFallbackDayRow(aiRequest, schedule, representativeRegion, date);
                    date = date.plusDays(1);
                    continue;
                }

                boolean savedAny = false;

                for (ScheduleDetail detail : dayDetails) {

                    // =========================
                    // A) "날씨=City", "대기질=Region" 기준 세팅
                    // =========================

                    // ✅ detail에 들어있는 FK
                    Long cityId = detail.getCityId();
                    Long regionId = detail.getRegionId();

                    // ✅ cityRef
                    City cityRef = null;
                    if (cityId != null) {
                        cityRef = em.getReference(City.class, cityId);
                    }

                    // ✅ rowRegion (대기질용): detail.regionId 우선, 없으면 대표 region
                    Region rowRegion =
                            (regionId != null)
                                    ? em.getReference(Region.class, regionId)
                                    : representativeRegion;

                    // ✅ 날씨 좌표: "City 우선"
                    //    (City 좌표가 없으면 Place 좌표, 마지막으로 Region 좌표)
                    Double lat = null;
                    Double lng = null;

                    // 1) City 좌표 우선
                    if (cityRef != null) {
                        try {
                            if (cityRef.getLat() != null && cityRef.getLng() != null) {
                                lat = cityRef.getLat();
                                lng = cityRef.getLng();
                            }
                        } catch (Exception ignored) {}
                    }

                    // 2) City 좌표 없으면 Place 좌표 (있으면 사용)
                    if (lat == null || lng == null) {
                        Places place = detail.getPlace();
                        Double pLat = extractDoubleByGetterCandidates(place,
                                "getLat", "getLatitude", "getY", "getMapY", "getMapy");
                        Double pLng = extractDoubleByGetterCandidates(place,
                                "getLng", "getLongitude", "getX", "getMapX", "getMapx");
                        if (pLat != null && pLng != null) {
                            lat = pLat;
                            lng = pLng;
                        }
                    }

                    // 3) 마지막 fallback: 대표 region 좌표
                    if (lat == null || lng == null) {
                        if (representativeRegion.getLat() != null && representativeRegion.getLng() != null) {
                            lat = representativeRegion.getLat();
                            lng = representativeRegion.getLng();
                        } else {
                            // 좌표 확보 불가 -> 이 detail은 분석 불가
                            continue;
                        }
                    }

                    // ✅ REGION_NAME 저장은 "광역 + 시군구" 형태
                    String regionName = buildAdministrativeRegionName(rowRegion, cityRef);

                    // =========================
                    // B) 날씨 / 대기질 조회
                    // =========================
                    // ✅ 날씨는 lat/lng (City 기반 좌표)로 조회
                    WeatherResult weather = weatherProvider.getDailyWeather(date, lat, lng);

                    // ✅ 대기질은 Region(광역)으로만 조회
                    AirQualityResult air = null;
                    try {
                        if (rowRegion != null) {
                            air = airQualityProvider.getAirQuality(rowRegion, date);
                        }
                    } catch (Exception ignored) {
                    }

                    // =========================
                    // ✅ 기상 수치 추출
                    // =========================
                    Double minTemp = (weather != null) ? weather.getMinTemp() : null;
                    Double maxTemp = (weather != null) ? weather.getMaxTemp() : null;
                    Double windSpeed = (weather != null) ? weather.getWindSpeed() : null;
                    Double precipitation = (weather != null) ? weather.getPrecipitation() : null;
                    Double rainProb = (weather != null) ? weather.getRainProb() : null;

                    // ✅ 대기질: 등급만 사용
                    String airGrade = (air != null) ? air.getGrade() : null;

                    // =========================
                    // C) 위험도
                    // =========================
                    RiskDecision decision = calculateRisk(weather, air);

                    // =========================
                    // D) 프롬프트
                    // =========================
                    String basePrompt = WeatherPromptBuilder.build(
                            date,
                            rowRegion,
                            weather,
                            air,
                            decision.level()
                    );

                    String weatherType = normalizeWeatherType(detail.getStopType());

                    String prompt = basePrompt
                            + "\n[일정 지점 정보]\n"
                            + "- detailId: " + detail.getDetailId() + "\n"
                            + "- stopType: " + (weatherType == null ? "" : weatherType) + "\n"
                            + "- regionName: " + (regionName == null ? "" : regionName) + "\n"
                            + "- regionId: " + (regionId == null ? "" : regionId) + "\n"
                            + "- cityId: " + (cityId == null ? "" : cityId) + "\n"
                            + "- lat/lng: " + lat + ", " + lng + "\n"
                            + "- windSpeed: " + windSpeed + "\n"
                            + "- precipitation: " + precipitation + "\n"
                            + "- rainProb: " + rainProb + "\n"
                            + "- temp(min~max): " + minTemp + "~" + maxTemp + "\n"
                            + "- airGrade: " + (airGrade == null ? "" : airGrade) + "\n";

                    String aiMessage;
                    try {
                        aiMessage = aiWeatherAiService.generateMessage(prompt);
                    } catch (Exception e) {
                        aiMessage = decision.message();
                    }

                    // =========================
                    // E) 저장
                    // =========================
                    AiWeather aiWeather = new AiWeather();
                    aiWeather.setAiRequest(aiRequest);
                    aiWeather.setSchedule(schedule);
                    aiWeather.setScheduleDetail(detail);
                    aiWeather.setTargetDate(date);

                    aiWeather.setLat(lat);
                    aiWeather.setLng(lng);

                    // ✅ 대기질은 region(광역)용, route 요약은 city용
                    aiWeather.setRegion(rowRegion);
                    aiWeather.setCity(cityRef);

                    aiWeather.setRegionName(regionName);
                    aiWeather.setWeatherType(weatherType);

                    // ✅ 기상 수치 저장
                    aiWeather.setTempMin(minTemp);
                    aiWeather.setTempMax(maxTemp);
                    aiWeather.setWindSpeed(windSpeed);
                    aiWeather.setPrecipitation(precipitation);
                    aiWeather.setPrecipProb(rainProb);

                    // ✅ 대기질: 등급만 저장
                    aiWeather.setAirGrade(airGrade);

                    aiWeather.setRiskLevel(decision.level().name());
                    aiWeather.setRiskReason(decision.reason());
                    aiWeather.setAiMessage(aiMessage);

                    aiWeatherRepository.save(aiWeather);
                    savedAny = true;
                }

                if (!savedAny) {
                    saveFallbackDayRow(aiRequest, schedule, representativeRegion, date);
                }

                date = date.plusDays(1);
            }

            long latency = System.currentTimeMillis() - startTime;
            aiLogService.logSuccess(aiRequest.getRequestId(), latency);

        } catch (Exception e) {
            long latency = System.currentTimeMillis() - startTime;
            aiLogService.logFail(aiRequest.getRequestId(), latency, e.getMessage());
            throw e;
        }
    }

    /* =========================
       fallback: 대표 Region으로 하루 1건 저장
       - detail이 없는 날(혹은 저장 못한 날)
       - 어차피 "DAY 카드"용이니 region 기준으로 OK
    ========================= */
    private void saveFallbackDayRow(
            AiRequest aiRequest, Schedule schedule, Region region, LocalDate date
    ) {

        if (region.getLat() == null || region.getLng() == null) {
            return;
        }

        WeatherResult weather = weatherProvider.getDailyWeather(date, region.getLat(), region.getLng());

        AirQualityResult air = null;
        try {
            air = airQualityProvider.getAirQuality(region, date);
        } catch (Exception ignored) {
        }

        Double minTemp = (weather != null) ? weather.getMinTemp() : null;
        Double maxTemp = (weather != null) ? weather.getMaxTemp() : null;
        Double windSpeed = (weather != null) ? weather.getWindSpeed() : null;
        Double precipitation = (weather != null) ? weather.getPrecipitation() : null;
        Double rainProb = (weather != null) ? weather.getRainProb() : null;

        String airGrade = (air != null) ? air.getGrade() : null;

        RiskDecision decision = calculateRisk(weather, air);

        String prompt = WeatherPromptBuilder.build(
                date,
                region,
                weather,
                air,
                decision.level()
        );

        String aiMessage;
        try {
            aiMessage = aiWeatherAiService.generateMessage(prompt);
        } catch (Exception e) {
            aiMessage = decision.message();
        }

        AiWeather aiWeather = new AiWeather();
        aiWeather.setAiRequest(aiRequest);
        aiWeather.setSchedule(schedule);
        aiWeather.setTargetDate(date);

        aiWeather.setLat(region.getLat());
        aiWeather.setLng(region.getLng());

        aiWeather.setRegion(region);
        aiWeather.setCity(null);

        aiWeather.setRegionName(region.getRegionName());
        aiWeather.setWeatherType("DAY");

        aiWeather.setTempMin(minTemp);
        aiWeather.setTempMax(maxTemp);
        aiWeather.setWindSpeed(windSpeed);
        aiWeather.setPrecipitation(precipitation);
        aiWeather.setPrecipProb(rainProb);

        aiWeather.setAirGrade(airGrade);

        aiWeather.setRiskLevel(decision.level().name());
        aiWeather.setRiskReason(decision.reason());
        aiWeather.setAiMessage(aiMessage);

        aiWeatherRepository.save(aiWeather);
    }

    private Region getRepresentativeRegion(Schedule schedule) {
        return schedule.getRegion() != null ? schedule.getRegion() : schedule.getStartRegion();
    }

    private int calcDayNumber(LocalDate startDate, LocalDate date) {
        return (int) (date.toEpochDay() - startDate.toEpochDay()) + 1;
    }

    /* =========================
       REGION_NAME 생성
    ========================= */
    private String buildAdministrativeRegionName(Region region, City city) {
        String regionName = (region != null ? region.getRegionName() : null);

        String cityName = null;
        if (city != null) {
            try {
                cityName = city.getCityName();
            } catch (Exception ignored) {
                cityName = null;
            }
        }

        if (regionName != null && cityName != null && !cityName.isBlank()) {
            return regionName + " " + cityName;
        }
        if (cityName != null && !cityName.isBlank()) {
            return cityName;
        }
        if (regionName != null && !regionName.isBlank()) {
            return regionName;
        }
        return null;
    }

    private String normalizeWeatherType(String stopType) {
        if (stopType == null) return null;
        String v = stopType.trim();
        return v.isEmpty() ? null : v;
    }

    /* =========================
       Places에서 Double getter 리플렉션 추출 (Places 구조가 확정이 아니라 유지)
    ========================= */
    private Double extractDoubleByGetterCandidates(Object target, String... getterNames) {
        if (target == null || getterNames == null) return null;

        for (String getter : getterNames) {
            Object val = invokeGetter(target, getter);
            Double d = toDouble(val);
            if (d != null) return d;
        }
        return null;
    }

    private Object invokeGetter(Object target, String getterName) {
        try {
            Method m = target.getClass().getMethod(getterName);
            return m.invoke(target);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Double toDouble(Object v) {
        if (v == null) return null;
        if (v instanceof Double d) return d;
        if (v instanceof Float f) return (double) f;
        if (v instanceof Integer i) return (double) i;
        if (v instanceof Long l) return (double) l;
        if (v instanceof String s) {
            try {
                return Double.valueOf(s.trim());
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    /* =========================
       위험도 계산 로직
    ========================= */
    private RiskDecision calculateRisk(WeatherResult weather, AirQualityResult air) {
        RiskLevel wind = weather == null ? RiskLevel.SAFE : riskByWind(weather.getWindSpeed());
        RiskLevel rain = weather == null ? RiskLevel.SAFE : riskByRain(weather.getPrecipitation());
        RiskLevel dust = air == null ? RiskLevel.SAFE : RiskLevel.fromAirGrade(air.getGrade());

        RiskLevel finalLevel = maxLevel(wind, rain, dust);

        return new RiskDecision(
                finalLevel,
                buildReason(weather, air),
                defaultMessage(finalLevel)
        );
    }

    private RiskLevel maxLevel(RiskLevel a, RiskLevel b, RiskLevel c) {
        RiskLevel max = a;
        if (b.ordinal() > max.ordinal()) max = b;
        if (c.ordinal() > max.ordinal()) max = c;
        return max;
    }

    private RiskLevel riskByWind(Double wind) {
        if (wind == null) return RiskLevel.SAFE;
        if (wind < 3.0) return RiskLevel.VERY_SAFE;
        if (wind < 6.0) return RiskLevel.SAFE;
        if (wind < 9.0) return RiskLevel.CAUTION;
        if (wind < 12.0) return RiskLevel.WARNING;
        return RiskLevel.DANGER;
    }

    private RiskLevel riskByRain(Double rain) {
        if (rain == null || Objects.equals(rain, 0.0)) return RiskLevel.VERY_SAFE;
        if (rain < 1.0) return RiskLevel.SAFE;
        if (rain < 5.0) return RiskLevel.CAUTION;
        if (rain < 10.0) return RiskLevel.WARNING;
        return RiskLevel.DANGER;
    }

    private String buildReason(WeatherResult weather, AirQualityResult air) {
        StringBuilder sb = new StringBuilder();

        if (weather != null) {
            if (weather.getWindSpeed() != null)
                sb.append("풍속 ").append(weather.getWindSpeed()).append("m/s ");
            if (weather.getPrecipitation() != null && weather.getPrecipitation() > 0)
                sb.append("강수 ").append(weather.getPrecipitation()).append("mm ");
            if (weather.getRainProb() != null)
                sb.append("강수확률 ").append(weather.getRainProb()).append("% ");
            if (weather.getMinTemp() != null && weather.getMaxTemp() != null)
                sb.append("기온 ").append(weather.getMinTemp()).append("~").append(weather.getMaxTemp()).append("℃ ");
        }

        if (air != null) {
            if (air.getGrade() != null && !air.getGrade().isBlank())
                sb.append("대기질 ").append(air.getGrade()).append(" ");
        }

        return sb.toString().trim();
    }

    private String defaultMessage(RiskLevel level) {
        return switch (level) {
            case VERY_SAFE -> "자전거 여행에 매우 적합한 날씨입니다.";
            case SAFE -> "대체로 안정적인 날씨로 라이딩이 가능합니다.";
            case CAUTION -> "일부 구간에서 주의가 필요한 날씨입니다.";
            case WARNING -> "날씨나 대기질로 인해 코스 조정이나 시간 변경을 권장합니다.";
            case DANGER -> "강한 기상 위험으로 라이딩 연기를 강력히 권장합니다.";
        };
    }

    @Transactional(readOnly = true)
    public AiWeatherRouteDTO.Response getLatestRouteWeather(Long scheduleId, Long userNo) {
        scheduleService.getScheduleEntity(scheduleId, userNo);

        List<AiWeather> list = aiWeatherRepository.findLatestByScheduleIdOrderByDetail(scheduleId);

        var byDay = list.stream()
                .filter(w -> w.getScheduleDetail() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        w -> w.getScheduleDetail().getDayNumber(),
                        java.util.LinkedHashMap::new,
                        java.util.stream.Collectors.toList()
                ));

        List<AiWeatherRouteDTO.DayRouteWeather> days = new ArrayList<>();

        for (var dayEntry : byDay.entrySet()) {

            Integer dayNumber = dayEntry.getKey();
            List<AiWeather> dayRows = dayEntry.getValue();

            LocalDate date = dayRows.get(0).getTargetDate();

            var byCity = dayRows.stream()
                    .filter(w -> w.getCity() != null)
                    .collect(groupingBy(
                            w -> w.getCity().getCityId(),
                            LinkedHashMap::new,
                            toList()
                    ));

            List<AiWeatherRouteDTO.CityRouteWeather> cities = new ArrayList<>();

            for (var cityEntry : byCity.entrySet()) {

                Long cityId = cityEntry.getKey();
                List<AiWeather> cityRows = cityEntry.getValue();

                RiskLevel maxLevel = cityRows.stream()
                        .map(r -> RiskLevel.valueOf(r.getRiskLevel()))
                        .max(Enum::compareTo)
                        .orElse(RiskLevel.SAFE);

                List<Integer> orders = cityRows.stream()
                        .map(r -> r.getScheduleDetail().getOrderInDay())
                        .sorted()
                        .toList();

                List<String> reasons = cityRows.stream()
                        .map(AiWeather::getRiskReason)
                        .filter(r -> r != null && !r.isBlank())
                        .distinct()
                        .toList();

                String regionName = cityRows.get(0).getRegionName();

                Double windSpeed = cityRows.stream()
                        .map(AiWeather::getWindSpeed)
                        .filter(Objects::nonNull)
                        .max(Double::compareTo)
                        .orElse(null);

                Double precipitation = cityRows.stream()
                        .map(AiWeather::getPrecipitation)
                        .filter(Objects::nonNull)
                        .max(Double::compareTo)
                        .orElse(null);

                Double precipProb = cityRows.stream()
                        .map(AiWeather::getPrecipProb)
                        .filter(Objects::nonNull)
                        .max(Double::compareTo)
                        .orElse(null);

                Double tempMax = cityRows.stream()
                        .map(AiWeather::getTempMax)
                        .filter(Objects::nonNull)
                        .max(Double::compareTo)
                        .orElse(null);

                Double tempMin = cityRows.stream()
                        .map(AiWeather::getTempMin)
                        .filter(Objects::nonNull)
                        .min(Double::compareTo)
                        .orElse(null);

                String airGrade = cityRows.stream()
                        .map(AiWeather::getAirGrade)
                        .filter(s -> s != null && !s.isBlank())
                        .findFirst()
                        .orElse(null);

                cities.add(
                        new AiWeatherRouteDTO.CityRouteWeather(
                                cityId,
                                regionName,
                                orders,

                                maxLevel.name(),
                                maxLevel.getLabel(),
                                maxLevel.getColor(),
                                maxLevel.getIcon(),
                                reasons,

                                windSpeed,
                                precipitation,
                                precipProb,
                                tempMax,
                                tempMin,

                                airGrade
                        )
                );
            }

            days.add(new AiWeatherRouteDTO.DayRouteWeather(dayNumber, date, cities));
        }

        return new AiWeatherRouteDTO.Response(scheduleId, days);
    }

    private record RiskDecision(RiskLevel level, String reason, String message) {
    }
}
