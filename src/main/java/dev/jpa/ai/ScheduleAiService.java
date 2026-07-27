package dev.jpa.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;

import dev.jpa.places.Places;
import dev.jpa.schedule.Schedule;
import dev.jpa.schedule.ScheduleRepository;
import dev.jpa.schedule_detail.ScheduleDetail;
import dev.jpa.schedule_detail.ScheduleDetailRepository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ScheduleAiService {

    private final RestClient openAi;
    private final ObjectMapper om;
    private final ScheduleRepository scheduleRepo;
    private final ScheduleDetailRepository detailRepo;
    private final KakaoLocalClient kakaoLocal;

    @Value("${openai.model:gpt-4.1-mini}")
    private String model;

    @Value("${kakao.reco.radius:1500}")
    private int recoRadius;

    @Value("${kakao.reco.size:15}")
    private int recoSize;

    public ScheduleAiService(
            @Qualifier("openAiRestClient") RestClient openAiRestClient,
            ObjectMapper objectMapper,
            ScheduleRepository scheduleRepository,
            ScheduleDetailRepository scheduleDetailRepository,
            KakaoLocalClient kakaoLocalClient
    ) {
        this.openAi = openAiRestClient;
        this.om = objectMapper;
        this.scheduleRepo = scheduleRepository;
        this.detailRepo = scheduleDetailRepository;
        this.kakaoLocal = kakaoLocalClient;
    }

    /* =========================
       Public APIs
    ========================= */

    public ObjectNode summary(Long scheduleId, int force) {
        ObjectNode itinerary = buildItineraryJson(scheduleId);

        String sys = """
                너는 TRAVEL_LEAF 자전거 여행 일정 AI 어시스턴트야.
                입력된 itinerary JSON만 근거로 사용해.
                과장/추측 금지. 짧고 명확하게.
                """;

        String user = """
                아래 itinerary JSON을 읽고
                1) oneliner: 한 줄 요약(20자~40자)
                2) summary: 핵심 bullet 3~6개
                를 만들어줘.

                itinerary:
                %s
                """.formatted(itinerary.toString());

        ObjectNode schema = schemaSummary();
        return callResponsesJsonSchema(sys, user, "ScheduleSummary", schema);
    }

    public ObjectNode hashtags(Long scheduleId, int force) {
        ObjectNode itinerary = buildItineraryJson(scheduleId);

        String sys = """
                너는 TRAVEL_LEAF 일정 해시태그 생성기야.
                입력된 itinerary JSON만 근거로.
                한국어 해시태그 형태로, 트렌디하지만 과장 금지.
                """;

        String user = """
                itinerary JSON을 읽고 해시태그 8~15개 생성해줘.
                규칙:
                - 각 항목은 "#..." 형태
                - 중복 금지
                - 너무 일반적인 태그(#여행)만 잔뜩 금지, 구체적으로

                itinerary:
                %s
                """.formatted(itinerary.toString());

        ObjectNode schema = schemaHashtags();
        return callResponsesJsonSchema(sys, user, "ScheduleHashtags", schema);
    }

    public ObjectNode dayHighlights(Long scheduleId, int force) {
        ObjectNode itinerary = buildItineraryJson(scheduleId);

        String sys = """
                너는 TRAVEL_LEAF 일정 하이라이트 요약기야.
                입력 itinerary JSON만 근거로.
                각 일차(day)마다:
                - highlight: 한 줄 요약
                - points: 포인트 2~4개
                를 만든다.
                """;

        String user = """
                itinerary JSON을 일차별로 분석해서 하이라이트를 만들어줘.

                itinerary:
                %s
                """.formatted(itinerary.toString());

        ObjectNode schema = schemaDayHighlights();
        return callResponsesJsonSchema(sys, user, "ScheduleDayHighlights", schema);
    }

    // ✅ NEW: 준비물/주의사항 (Prep)
    public ObjectNode prep(Long scheduleId, int force) {
        ObjectNode itinerary = buildItineraryJson(scheduleId);

        String sys = """
                너는 TRAVEL_LEAF 자전거 여행 '준비물/주의사항' 체크리스트 생성기야.
                입력된 itinerary JSON만 근거로 작성해.
                - 기본 자전거 여행에서 항상 필요한 준비물(헬멧/라이트 등)은 포함 가능(과장 금지).
                - itinerary에 근거가 없는 날씨/통제/특정 장소 정보는 단정하지 말고 일반 주의로 표현해.
                - 항목은 짧고 체크리스트에 어울리게 작성해.
                """;

        String user = """
                아래 itinerary를 읽고, 여행 시작 전에 바로 확인 가능한 체크리스트를 만들어줘.

                출력 규칙:
                1) summary: 한 줄 요약(20~40자)
                2) packing: 준비물 6~12개 (중복X, 짧게)
                3) cautions: 주의사항 3~8개 (중복X, 짧게)
                4) riskLevel: "LOW"|"MID"|"HIGH"
                   - requestDifficulty가 '고급'이면 HIGH, '중급'이면 MID, '초급'이면 LOW
                   - 불명확하면 MID

                itinerary:
                %s
                """.formatted(itinerary.toString());

        ObjectNode schema = schemaPrep();
        return callResponsesJsonSchema(sys, user, "SchedulePrepChecklist", schema);
    }

    /**
     * ✅ chat 개선:
     * - 일반 Q&A: itinerary 기반 답변
     * - "추천/주변/근처/가볼만/더 추천" 질문: schedule_detail 좌표(anchor) 기반으로 카카오 후보를 뽑아 추천
     *   (후보가 없거나 실패 시 폴백으로 일반 추천/검색 키워드 제공)
     */
    public ObjectNode chat(Long scheduleId, ObjectNode reqBody) {
        ObjectNode itinerary = buildItineraryJson(scheduleId);

        String sessionId = reqBody.path("sessionId").asText("");
        String message = reqBody.path("message").asText("");
        JsonNode userNo = reqBody.get("userNo"); // null 가능

        boolean wantReco = isRecommendationQuestion(message);
        int targetDay = extractDayFromMessage(message);
        if (targetDay <= 0) targetDay = 1;

        // ✅ 추천 모드면: 후보를 만들어서 LLM에게 "후보 중에서만" 고르게 함
        ArrayNode candidatesArr = om.createArrayNode();
        ObjectNode anchorNode = om.createObjectNode();

        if (wantReco) {
            try {
                RecommendationContext ctx = buildRecommendationContext(scheduleId, targetDay);
                if (ctx.anchorLat != null && ctx.anchorLng != null) {
                    anchorNode.put("lat", ctx.anchorLat);
                    anchorNode.put("lng", ctx.anchorLng);
                    anchorNode.put("day", targetDay);
                    anchorNode.put("anchorLabel", nv(ctx.anchorLabel));
                }

                for (KakaoLocalClient.PlaceDoc c : ctx.candidates) {
                    ObjectNode n = om.createObjectNode();
                    n.put("name", c.getName());
                    n.put("categoryName", nv(c.getCategoryName()));
                    n.put("address", nv(c.getAddress()));
                    n.put("roadAddress", nv(c.getRoadAddress()));
                    n.put("placeUrl", nv(c.getPlaceUrl()));
                    n.put("lat", c.getLat());
                    n.put("lng", c.getLng());
                    if (c.getDistanceM() != null) n.put("distanceM", c.getDistanceM());
                    candidatesArr.add(n);
                }

                // 후보가 너무 없으면: 폴백 답변을 바로 리턴 (LLM hallucination 방지)
                if (candidatesArr.size() < 5) {
                    return fallbackRecoAnswer(itinerary, targetDay, ctx);
                }
            } catch (Exception e) {
                // 카카오 키 없거나 네트워크 문제 등 -> 폴백
                return fallbackRecoAnswer(itinerary, targetDay, null);
            }
        }

        // ✅ 시스템 프롬프트: 추천 모드 설명 추가 + 일반 Q&A 규칙 유지
        String sys = """
                너는 TRAVEL_LEAF 공유일정 Q&A 챗봇이다.

                [규칙]
                1) 일반 질문(일정/코스/날짜/장소 확인)은 itinerary JSON을 근거로만 답한다.
                   itinerary에 없는 정보는 "일정 데이터에는 없어요"라고 말하되,
                   대신 사용자가 다음 행동을 할 수 있게 한 줄 안내를 덧붙여라.
                2) 사용자가 "추천/주변/근처/가볼만한/더 추천"처럼 추가 장소 추천을 요구하면,
                   제공된 candidates(후보 리스트)가 있으면 그 후보 중에서만 추천한다. (후보 밖 새 장소 이름 생성 금지)
                   후보가 비어있다면, 지역/도시/코스 맥락 기반으로 '추천 카테고리/검색 키워드'를 제시한다.
                3) 출력은 반드시 JSON 스키마에 맞춘다:
                   { "answer": string, "sources": [ { "day": int, "placeName": string }, ... ] }

                [추천모드 출력 가이드]
                - answer에는 추천 5개를 번호로 정리하고(이유 1줄씩),
                  "어디에 끼우면 좋은지(코스 중간/끝/식사)" 한 줄도 포함해라.
                - sources에는 추천한 placeName들을 넣어라(추천이면 day=targetDay).
                """;

        String user = """
                [sessionId]=%s
                [userNo]=%s
                [targetDay]=%d
                질문: %s

                itinerary:
                %s

                anchor(추천 기준점, 없을 수 있음):
                %s

                candidates(추천 후보 리스트, 없을 수 있음):
                %s
                """.formatted(
                sessionId,
                (userNo == null ? "null" : userNo.toString()),
                targetDay,
                message,
                itinerary.toString(),
                anchorNode.toString(),
                candidatesArr.toString()
        );

        ObjectNode schema = schemaChat();
        return callResponsesJsonSchema(sys, user, "ScheduleChat", schema);
    }

    // /api/ai/schedules/preview/hashtags
    public ObjectNode hashtagsFromItinerary(ObjectNode itinerary, int force) {
        String sys = """
                너는 TRAVEL_LEAF 일정 해시태그 생성기야.
                입력된 itinerary JSON만 근거로.
                한국어 해시태그 형태로, 트렌디하지만 과장 금지.
                """;

        String user = """
                itinerary JSON을 읽고 해시태그 8~15개 생성해줘.
                규칙:
                - 각 항목은 "#..." 형태
                - 중복 금지
                - 너무 일반적인 태그(#여행)만 잔뜩 금지, 구체적으로

                itinerary:
                %s
                """.formatted(itinerary == null ? "{}" : itinerary.toString());

        ObjectNode schema = schemaHashtags();
        return callResponsesJsonSchema(sys, user, "ScheduleHashtagsPreview", schema);
    }

    /* =========================
       Recommendation (Kakao 기반) Helpers
    ========================= */

    private static class RecommendationContext {
        Double anchorLat;
        Double anchorLng;
        String anchorLabel;
        List<KakaoLocalClient.PlaceDoc> candidates = new ArrayList<>();
        List<SimplePlace> existingPlaces = new ArrayList<>();
        String regionName;
        String cityName;
        String dayCourseSummary;
    }

    private static class SimplePlace {
        String name;
        Double lat;
        Double lng;

        SimplePlace(String name, Double lat, Double lng) {
            this.name = name;
            this.lat = lat;
            this.lng = lng;
        }
    }

    private RecommendationContext buildRecommendationContext(Long scheduleId, int day) {
        RecommendationContext ctx = new RecommendationContext();

        // 일정(지역/도시명 폴백용)
        Schedule s = scheduleRepo.findByIdWithJoins(scheduleId)
                .orElseGet(() -> scheduleRepo.findById(scheduleId)
                        .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + scheduleId)));

        Object region = s.getRegion();
        Object city = s.getCity();
        ctx.regionName = nv(asString(invokeAny(region, "getRegionName", "getName")));
        ctx.cityName = nv(asString(invokeAny(city, "getCityName", "getName")));

        // day 상세 목록(리포지토리 메서드가 day용이 없어서 전체를 가져와 필터)
        List<ScheduleDetail> all =
                detailRepo.findBySchedule_ScheduleIdOrderByDayNumberAscOrderInDayAsc(scheduleId);

        List<ScheduleDetail> dayDetails = new ArrayList<>();
        for (ScheduleDetail d : (all == null ? List.<ScheduleDetail>of() : all)) {
            int dn = d.getDayNumber() == null ? 1 : d.getDayNumber();
            if (dn == day) dayDetails.add(d);
        }

        // existing(중복 제거용) + 코스 요약
        StringBuilder daySb = new StringBuilder();
        for (ScheduleDetail d : dayDetails) {
            Places p = d.getPlace();
            String placeName = firstNonBlank(d.getPlaceName(), (p != null ? p.getName() : null), "장소");
            Double[] ll = getLatLngFromDetailOrPlace(d, p);

            ctx.existingPlaces.add(new SimplePlace(placeName, ll[0], ll[1]));
            daySb.append(placeName).append(" / ");
        }
        ctx.dayCourseSummary = daySb.length() > 3 ? daySb.substring(0, daySb.length() - 3) : daySb.toString();

        // anchor 선택: END > MID > START > 그 외 마지막 좌표
        SimplePlace anchor = pickAnchor(dayDetails);
        if (anchor != null && anchor.lat != null && anchor.lng != null) {
            ctx.anchorLat = anchor.lat;
            ctx.anchorLng = anchor.lng;
            ctx.anchorLabel = anchor.name;
        }

        // anchor 없으면 후보를 못 뽑음(폴백으로 갈 것)
        if (ctx.anchorLat == null || ctx.anchorLng == null) {
            return ctx;
        }

        // 카카오 후보: AT4(관광) + FD6(음식) + CE7(카페)
        // 1차: radius=recoRadius, 2차: 후보 부족하면 radius 3000 확장
        List<KakaoLocalClient.PlaceDoc> merged = new ArrayList<>();
        merged.addAll(kakaoLocal.searchByCategory("AT4", ctx.anchorLat, ctx.anchorLng, recoRadius, recoSize));
        merged.addAll(kakaoLocal.searchByCategory("FD6", ctx.anchorLat, ctx.anchorLng, recoRadius, recoSize));
        merged.addAll(kakaoLocal.searchByCategory("CE7", ctx.anchorLat, ctx.anchorLng, recoRadius, recoSize));

        if (merged.size() < 12) {
            int r2 = Math.max(2500, recoRadius * 2);
            merged.addAll(kakaoLocal.searchByCategory("AT4", ctx.anchorLat, ctx.anchorLng, r2, recoSize));
            merged.addAll(kakaoLocal.searchByCategory("FD6", ctx.anchorLat, ctx.anchorLng, r2, recoSize));
            merged.addAll(kakaoLocal.searchByCategory("CE7", ctx.anchorLat, ctx.anchorLng, r2, recoSize));
        }

        // 중복 제거 + 일정에 이미 있는 곳 제외
        Map<String, KakaoLocalClient.PlaceDoc> uniq = new LinkedHashMap<>();
        for (KakaoLocalClient.PlaceDoc c : merged) {
            if (c == null) continue;
            uniq.putIfAbsent(c.normKey(), c);
        }

        List<KakaoLocalClient.PlaceDoc> filtered = new ArrayList<>();
        Set<String> existNames = new HashSet<>();
        for (SimplePlace ep : ctx.existingPlaces) {
            existNames.add(normName(ep.name));
        }

        for (KakaoLocalClient.PlaceDoc c : uniq.values()) {
            String nn = normName(c.getName());
            if (existNames.contains(nn)) continue;

            boolean nearDup = false;
            for (SimplePlace ep : ctx.existingPlaces) {
                if (ep.lat == null || ep.lng == null) continue;
                double m = haversineMeters(ep.lat, ep.lng, c.getLat(), c.getLng());
                if (m <= 80) { // 80m 이내면 같은 곳 취급
                    nearDup = true;
                    break;
                }
            }
            if (nearDup) continue;

            filtered.add(c);
            if (filtered.size() >= 25) break; // LLM 입력 과다 방지
        }

        ctx.candidates = filtered;
        return ctx;
    }

    private SimplePlace pickAnchor(List<ScheduleDetail> dayDetails) {
        if (dayDetails == null || dayDetails.isEmpty()) return null;

        SimplePlace bestEnd = null;
        SimplePlace bestMid = null;
        SimplePlace bestStart = null;
        SimplePlace lastAny = null;

        for (ScheduleDetail d : dayDetails) {
            Places p = d.getPlace();
            String placeName = firstNonBlank(d.getPlaceName(), (p != null ? p.getName() : null), "장소");
            Double[] ll = getLatLngFromDetailOrPlace(d, p);
            SimplePlace sp = new SimplePlace(placeName, ll[0], ll[1]);
            if (sp.lat == null || sp.lng == null || sp.lat == 0 || sp.lng == 0) continue;

            String st = nv(d.getStopType()).toUpperCase(Locale.ROOT);
            lastAny = sp;

            if ("END".equals(st)) bestEnd = sp;
            else if ("MID".equals(st)) bestMid = sp;
            else if ("START".equals(st)) bestStart = sp;
        }

        if (bestEnd != null) return bestEnd;
        if (bestMid != null) return bestMid;
        if (bestStart != null) return bestStart;
        return lastAny;
    }

    private ObjectNode fallbackRecoAnswer(ObjectNode itinerary, int day, RecommendationContext ctx) {
        // 후보가 없거나 카카오 실패 시: "없어요"로 끝내지 않고, 다음 행동 가능한 형태로 출력
        String regionCity = "";
        try {
            JsonNode sch = itinerary.path("schedule");
            String r = sch.path("regionName").asText("");
            String c = sch.path("cityName").asText("");
            regionCity = (r + " " + c).trim();
        } catch (Exception ignore) {}

        String course = (ctx != null ? nv(ctx.dayCourseSummary) : "");

        ObjectNode out = om.createObjectNode();
        StringBuilder sb = new StringBuilder();

        sb.append(day).append("일차 기준으로 주변 ‘실존 후보’를 충분히 못 가져왔어.\n");
        sb.append("대신 아래 방식으로 바로 추천 받아볼 수 있어:\n\n");
        sb.append("1) 검색 키워드 추천\n");
        if (!regionCity.isBlank()) {
            sb.append("- ").append(regionCity).append(" 한강 자전거 코스 전망 포인트\n");
            sb.append("- ").append(regionCity).append(" 라이더 카페 / 브런치 / 대여소 근처\n");
            sb.append("- ").append(regionCity).append(" 공원(피크닉) / 포토스팟 / 화장실 가까운 곳\n");
        } else {
            sb.append("- 한강 자전거 코스 전망 포인트\n");
            sb.append("- 라이더 카페 / 브런치 / 공원\n");
        }

        if (!course.isBlank()) {
            sb.append("\n2) 현재 코스 기반 추천 카테고리\n");
            sb.append("- 코스: ").append(course).append("\n");
            sb.append("- 코스 중간: 카페(CE7) / 편의시설 / 쉼터\n");
            sb.append("- 코스 끝: 식사(FD6) / 야경 포인트(AT4)\n");
        }

        sb.append("\n👉 카카오 키가 설정되면(백엔드 kakao.rest-api-key) 버튼 한 번 더 눌렀을 때 실제 장소명 5개로 추천해줄게!");

        out.put("answer", sb.toString());
        out.putArray("sources"); // 빈 배열 OK
        return out;
    }

    private boolean isRecommendationQuestion(String msg) {
        if (msg == null) return false;
        String m = msg.trim();
        if (m.isEmpty()) return false;
        return m.contains("추천")
                || m.contains("근처")
                || m.contains("주변")
                || m.contains("가볼만")
                || m.contains("가 볼만")
                || m.contains("더 ") && m.contains("추천")
                || (m.contains("추가") && m.contains("장소"));
    }

    private int extractDayFromMessage(String msg) {
        if (msg == null) return -1;
        Pattern p = Pattern.compile("(\\d+)\\s*일차");
        Matcher m = p.matcher(msg);
        if (m.find()) {
            try { return Integer.parseInt(m.group(1)); } catch (Exception ignore) {}
        }
        // “첫째날/둘째날” 같은 케이스를 아주 간단히 처리
        if (msg.contains("첫") && msg.contains("날")) return 1;
        if (msg.contains("둘") && msg.contains("날")) return 2;
        if (msg.contains("셋") && msg.contains("날")) return 3;
        return -1;
    }

    private String normName(String s) {
        if (s == null) return "";
        return s.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", "")
                .replaceAll("[()\\[\\]{}]", "");
    }

    private double haversineMeters(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return R * c;
    }

    private Double[] getLatLngFromDetailOrPlace(ScheduleDetail d, Places p) {
        // 1) detail 자체 lat/lng가 있으면 우선
        Double lat = asDouble(invokeAny(d, "getLat", "getLatitude", "getY"));
        Double lng = asDouble(invokeAny(d, "getLng", "getLongitude", "getX"));

        // 2) 없으면 place lat/lng
        if ((lat == null || lat == 0) && p != null && p.getLat() != null) lat = p.getLat();
        if ((lng == null || lng == 0) && p != null && p.getLng() != null) lng = p.getLng();

        // 유효성
        if (lat == null || lng == null) return new Double[]{null, null};
        if (!Double.isFinite(lat) || !Double.isFinite(lng)) return new Double[]{null, null};
        if (lat == 0 || lng == 0) return new Double[]{null, null};

        return new Double[]{lat, lng};
    }

    private Double asDouble(Object v) {
        if (v == null) return null;
        try {
            return Double.parseDouble(String.valueOf(v));
        } catch (Exception e) {
            return null;
        }
    }

    /* =========================
       Build Itinerary JSON (DB -> LLM input)
    ========================= */

    private ObjectNode buildItineraryJson(Long scheduleId) {
        Schedule s = scheduleRepo.findByIdWithJoins(scheduleId)
                .orElseGet(() -> scheduleRepo.findById(scheduleId)
                        .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + scheduleId)));

        List<ScheduleDetail> details =
                detailRepo.findBySchedule_ScheduleIdOrderByDayNumberAscOrderInDayAsc(scheduleId);

        ObjectNode root = om.createObjectNode();

        ObjectNode schedule = root.putObject("schedule");
        schedule.put("id", scheduleId);
        schedule.put("title", nv(s.getScheduleTitle()));
        schedule.put("startDate", s.getStartDate() == null ? "" : s.getStartDate().toString());
        schedule.put("endDate", s.getEndDate() == null ? "" : s.getEndDate().toString());
        schedule.put("startTime", nv(s.getStartTime()));
        schedule.put("endTime", nv(s.getEndTime()));
        schedule.put("peopleCount", s.getPeopleCount() == null ? 0 : s.getPeopleCount());
        schedule.put("budget", s.getBudget() == null ? 0 : s.getBudget());
        schedule.put("requestDifficulty", nv(s.getRequestDifficulty()));
        schedule.put("withType", nv(s.getWithType()));
        schedule.put("memo", nv(s.getMemo()));

        Object region = s.getRegion();
        Object city = s.getCity();
        schedule.put("regionId", asLong(invokeAny(region, "getRegionId", "getId")));
        schedule.put("cityId", asLong(invokeAny(city, "getCityId", "getId")));
        schedule.put("regionName", nv(asString(invokeAny(region, "getRegionName", "getName"))));
        schedule.put("cityName", nv(asString(invokeAny(city, "getCityName", "getName"))));

        Map<Integer, List<ScheduleDetail>> byDay = new LinkedHashMap<>();
        for (ScheduleDetail d : (details == null ? List.<ScheduleDetail>of() : details)) {
            Integer day = d.getDayNumber() == null ? 1 : d.getDayNumber();
            byDay.computeIfAbsent(day, k -> new ArrayList<>()).add(d);
        }

        ArrayNode daysArr = root.putArray("days");

        for (Map.Entry<Integer, List<ScheduleDetail>> e : byDay.entrySet()) {
            Integer dayNo = e.getKey();
            List<ScheduleDetail> dayDetails = e.getValue();

            ObjectNode dayNode = om.createObjectNode();
            dayNode.put("day", dayNo);

            ArrayNode placesArr = dayNode.putArray("places");

            for (ScheduleDetail d : dayDetails) {
                Places p = d.getPlace();

                String placeName = firstNonBlank(
                        d.getPlaceName(),
                        (p != null ? p.getName() : null),
                        "장소"
                );

                ObjectNode placeNode = om.createObjectNode();
                placeNode.put("orderInDay", d.getOrderInDay() == null ? 0 : d.getOrderInDay());
                placeNode.put("stopType", nv(d.getStopType()));
                placeNode.put("name", placeName);

                placeNode.put("category", p == null ? "" : nv(p.getCategory()));
                placeNode.put("address", p == null ? "" : nv(p.getAddress()));

                // ✅ detail lat/lng 우선 + 없으면 place lat/lng
                Double[] ll = getLatLngFromDetailOrPlace(d, p);
                placeNode.put("lat", (ll[0] == null ? 0 : ll[0]));
                placeNode.put("lng", (ll[1] == null ? 0 : ll[1]));

                placeNode.put("memo", nv(d.getMemo()));
                placeNode.put("cost", d.getCost() == null ? 0 : d.getCost());
                placeNode.put("distanceKM", d.getDistanceKM() == null ? 0 : d.getDistanceKM());
                placeNode.put("startTime", d.getStartTime() == null ? "" : d.getStartTime().toString());
                placeNode.put("endTime", d.getEndTime() == null ? "" : d.getEndTime().toString());

                placesArr.add(placeNode);
            }

            daysArr.add(dayNode);
        }

        return root;
    }

    /* =========================
       OpenAI Responses API call (JSON Schema)
    ========================= */

    private ObjectNode callResponsesJsonSchema(String systemPrompt, String userPrompt, String schemaName, ObjectNode jsonSchema) {
        ObjectNode body = om.createObjectNode();
        body.put("model", model);
        body.put("store", false);

        ArrayNode input = body.putArray("input");

        ObjectNode sys = input.addObject();
        sys.put("role", "system");
        sys.putArray("content")
                .addObject()
                .put("type", "input_text")
                .put("text", systemPrompt);

        ObjectNode usr = input.addObject();
        usr.put("role", "user");
        usr.putArray("content")
                .addObject()
                .put("type", "input_text")
                .put("text", userPrompt);

        ObjectNode text = body.putObject("text");
        ObjectNode format = text.putObject("format");
        format.put("type", "json_schema");
        format.put("name", schemaName);
        format.set("schema", jsonSchema);
        format.put("strict", true);

        JsonNode res = openAi.post()
                .uri("/responses")
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        String outText = extractOutputText(res);

        try {
            JsonNode parsed = om.readTree(outText);
            if (parsed != null && parsed.isObject()) {
                return (ObjectNode) parsed;
            }
        } catch (Exception ignore) {}

        ObjectNode fallback = om.createObjectNode();
        fallback.put("error", "AI output parse failed");
        fallback.put("raw", outText == null ? "" : outText);
        return fallback;
    }

    private String extractOutputText(JsonNode res) {
        if (res == null) return "";
        JsonNode output = res.get("output");
        if (output != null && output.isArray()) {
            StringBuilder sb = new StringBuilder();
            for (JsonNode item : output) {
                JsonNode content = item.get("content");
                if (content != null && content.isArray()) {
                    for (JsonNode c : content) {
                        JsonNode text = c.get("text");
                        if (text != null && text.isTextual()) sb.append(text.asText());
                    }
                }
            }
            String s = sb.toString().trim();
            if (!s.isEmpty()) return s;
        }
        JsonNode ot = res.get("output_text");
        if (ot != null && ot.isTextual()) return ot.asText();
        return res.toString();
    }

    /* =========================
       JSON Schemas
    ========================= */

    private ObjectNode schemaSummary() {
        ObjectNode root = om.createObjectNode();
        root.put("type", "object");
        root.put("additionalProperties", false);

        ObjectNode props = root.putObject("properties");
        props.putObject("oneliner").put("type", "string");

        ObjectNode summary = props.putObject("summary");
        summary.put("type", "array");
        summary.putObject("items").put("type", "string");
        summary.put("minItems", 3);
        summary.put("maxItems", 6);

        ArrayNode req = root.putArray("required");
        req.add("oneliner");
        req.add("summary");
        return root;
    }

    private ObjectNode schemaHashtags() {
        ObjectNode root = om.createObjectNode();
        root.put("type", "object");
        root.put("additionalProperties", false);

        ObjectNode props = root.putObject("properties");
        ObjectNode tags = props.putObject("hashtags");
        tags.put("type", "array");
        tags.putObject("items").put("type", "string");

        root.putArray("required").add("hashtags");
        return root;
    }

    private ObjectNode schemaDayHighlights() {
        ObjectNode root = om.createObjectNode();
        root.put("type", "object");
        root.put("additionalProperties", false);

        ObjectNode props = root.putObject("properties");
        ObjectNode days = props.putObject("days");
        days.put("type", "array");

        ObjectNode dayItem = days.putObject("items");
        dayItem.put("type", "object");
        dayItem.put("additionalProperties", false);

        ObjectNode dayProps = dayItem.putObject("properties");
        dayProps.putObject("day").put("type", "integer");
        dayProps.putObject("highlight").put("type", "string");

        ObjectNode points = dayProps.putObject("points");
        points.put("type", "array");
        points.putObject("items").put("type", "string");

        ArrayNode dayReq = dayItem.putArray("required");
        dayReq.add("day");
        dayReq.add("highlight");
        dayReq.add("points");

        root.putArray("required").add("days");
        return root;
    }

    private ObjectNode schemaChat() {
        ObjectNode root = om.createObjectNode();
        root.put("type", "object");
        root.put("additionalProperties", false);

        ObjectNode props = root.putObject("properties");
        props.putObject("answer").put("type", "string");

        ObjectNode sources = props.putObject("sources");
        sources.put("type", "array");

        ObjectNode srcItem = sources.putObject("items");
        srcItem.put("type", "object");
        srcItem.put("additionalProperties", false);

        ObjectNode srcProps = srcItem.putObject("properties");
        srcProps.putObject("day").put("type", "integer");
        srcProps.putObject("placeName").put("type", "string");

        ArrayNode srcReq = srcItem.putArray("required");
        srcReq.add("day");
        srcReq.add("placeName");

        ArrayNode req = root.putArray("required");
        req.add("answer");
        req.add("sources");

        return root;
    }

    // ✅ NEW: Prep schema
    private ObjectNode schemaPrep() {
        ObjectNode root = om.createObjectNode();
        root.put("type", "object");
        root.put("additionalProperties", false);

        ObjectNode props = root.putObject("properties");

        props.putObject("summary").put("type", "string");

        ObjectNode packing = props.putObject("packing");
        packing.put("type", "array");
        packing.putObject("items").put("type", "string");
        packing.put("minItems", 6);
        packing.put("maxItems", 12);

        ObjectNode cautions = props.putObject("cautions");
        cautions.put("type", "array");
        cautions.putObject("items").put("type", "string");
        cautions.put("minItems", 3);
        cautions.put("maxItems", 8);

        ObjectNode risk = props.putObject("riskLevel");
        risk.put("type", "string");
        risk.putArray("enum").add("LOW").add("MID").add("HIGH");

        root.putArray("required").add("summary").add("packing").add("cautions").add("riskLevel");
        return root;
    }

    /* =========================
       Helpers
    ========================= */

    private String nv(String s) {
        return s == null ? "" : s;
    }

    private String firstNonBlank(String... arr) {
        if (arr == null) return "";
        for (String s : arr) {
            if (s != null && !s.trim().isEmpty()) return s.trim();
        }
        return "";
    }

    private Object invokeAny(Object target, String... methodNames) {
        if (target == null || methodNames == null) return null;
        for (String name : methodNames) {
            try {
                Method m = target.getClass().getMethod(name);
                return m.invoke(target);
            } catch (Exception ignore) {}
        }
        return null;
    }

    private String asString(Object v) {
        return v == null ? "" : String.valueOf(v);
    }

    private long asLong(Object v) {
        if (v == null) return 0;
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (Exception e) {
            return 0;
        }
    }
}
