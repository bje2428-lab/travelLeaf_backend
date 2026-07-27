package dev.jpa.ai_plan;

import dev.jpa.checklist_batch.ChecklistBatch;
import dev.jpa.checklist_user.ChecklistUser;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class AiPlanPromptBuilder {

    private AiPlanPromptBuilder() {}

    /** 호환용 오버로드 (기존 호출부 방어) */
    public static String build(
            ChecklistBatch batch,
            List<String> activityNames
    ) {
        return build(batch, List.of(), activityNames);
    }

    /** 메인 */
    public static String build(
            ChecklistBatch batch,
            List<ChecklistUser> checklistUsers,
            List<String> activityNames
    ) {
        if (batch == null) {
            throw new IllegalArgumentException("ChecklistBatch is null");
        }

        String startDatetime = safe(batch.getStartDatetime()); // yyyy-MM-ddTHH:mm
        String endDatetime   = safe(batch.getEndDatetime());

        String startDate = startDatetime.length() >= 10 ? startDatetime.substring(0, 10) : "";
        String startTime = startDatetime.length() >= 16 ? startDatetime.substring(11, 16) : "";

        String endDate = endDatetime.length() >= 10 ? endDatetime.substring(0, 10) : "";
        String endTime = endDatetime.length() >= 16 ? endDatetime.substring(11, 16) : "";

        int travelDays = calcDaysInclusive(startDate, endDate);

        String startPoint = safe(batch.getStartPoint());
        String endPoint   = safe(batch.getEndPoint());

        // DB에 JSON 문자열로 저장돼 있는 값들
        String routeRegionsJson   = normalizeJsonArrayString(batch.getRouteRegions());
        String routeCitiesJson    = normalizeJsonArrayString(batch.getRouteCities());
        String routeWaypointsJson = normalizeJsonArrayString(batch.getRouteWaypoints());

        String activityText = (activityNames == null || activityNames.isEmpty())
                ? "(없음)"
                : String.join(", ", activityNames);

        // ✅ Stay 판단은 지금 정보만으로 확정 불가 → 임의작성 금지 원칙 때문에 미확인 유지
        String wantStayText = "(미확인)";

        // 프런트 selectedSummary 느낌 유지 (표시/참고용)
        String selectedSummary = buildSelectedSummary(
                routeRegionsJson, routeCitiesJson,
                startDate, startTime, endDate, endTime, travelDays,
                startPoint, endPoint,
                routeWaypointsJson,
                activityText,
                wantStayText
        );

        return """
당신은 "현실적인 자전거 여행 일정"을 작성하는 AI이자
실제 자전거 동호인들이 참고해도 될 수준의
"구체적인 라이딩 루트 추천자"입니다.

(중요) placeName은 사용자가 그대로 따라갈 수 있도록 "구체적인 장소명"으로 작성하세요.
- WAYPOINT / STAY 는 반드시 '실제 상호명(가게/카페/식당/숙소/관광지 이름)'으로 작성
  예) "카페 노티드 판교점", "성수동 대림창고", "스타벅스 리저브 더현대서울"
- "판교역 근처 브런치 카페" 같은 뭉뚱그린 표현 금지
- 장소가 뭘 하는 곳인지 메모에 의존해서 유저가 추측하게 만들지 마세요.
  placeName 자체가 '어딜 가야 하는지' 명확해야 합니다.

[출력 규칙]
- 반드시 JSON만 출력
- 최상위에 summary(짧은 요약 문장, 1~2문장)를 포함
- 설명 문장, 마크다운, 코드블록 출력 금지

- ✅ 반드시 최상위에 travelInfo 객체를 포함 (필수)
- ✅ travelInfo에는 아래 입력값을 "그대로" 복사하여 포함 (값 변경/가공 금지)
  - routeRegions: %s
  - routeCities: %s
  - routeWaypoints: %s
  - startPoint: "%s"
  - endPoint: "%s"
  - startDatetime: "%s"
  - endDatetime: "%s"

[입력(사용자 여행 정보)]
%s

[시간 개념 (⚠️ 매우 중요)]
- 여행은 %s %s 에 시작
- 여행은 %s %s 에 종료
- 모든 시간은 "HH:mm" 문자열
- 1일차 일정은 여행 시작 시각 이후부터 구성
- 마지막 날 일정은 여행 종료 시각(%s) 이전까지 "꽉 채워서" 구성 (공백 방치 금지)
- ❗ WAYPOINT / STAY 는 startTime + endTime 둘 다 포함 (체류시간 필수)
- ❗ COURSE 는 startTime + endTime 둘 다 포함 (구간 주행시간 필수)

[라이딩 물리 계산 규칙]

- COURSE 소요시간(시간) = distanceKm ÷ 평균속도
- 평균속도:
  초급자 14km/h
  중급자 18km/h
  상급자 25km/h
  복수 선택 시 중간값 적용

- 계산된 시간과 실제 배치된 시간 차이는 ±10분 이내여야 함.
- 1시간에 30km 초과 이동 금지.
- 단일 COURSE 100km 초과 금지.

[휴식 의무 규칙]

- 90분 이상 연속 주행 금지.
- 25km 이상 COURSE 이후 반드시 WAYPOINT 1개 포함.
- 하루 최소 2회 이상 카페/식사/휴식 포함.
- 장거리 COURSE 연속 배치 금지.

[시간 연속성 강제 규칙]
- 모든 detail.startTime은 반드시 이전 detail.endTime과 동일해야 함.
- 임의로 시간을 건너뛰지 말 것.
- 30분 이상 공백이 발생하면 전체 일정은 무효로 간주됨.
- 마지막 detail의 endTime은 여행 종료 시각과 정확히 일치해야 함.

[중요: 여행 종료(END) 규칙]
- END 는 "전체 여행 최종 1회"만 존재해야 함 (중간 날짜에는 절대 END 금지)
- END 는 반드시 마지막 날(dayNumber=%d) 마지막 detail 이어야 함
- END.endTime 은 반드시 "%s" 로 설정 (여행 종료 시각과 정확히 일치)
- END 직전 detail.endTime은 END.startTime과 동일해야 함.
- 마지막 날도 다른 날과 동일하게 구체적 일정으로 구성.
- 마지막 날에는 STAY 생성 금지.

[날짜 분리 규칙 (중요)]
- dayNumber는 날짜 기준으로 반드시 분리
- 여행일수 = %d
- dayNumber는 1부터 %d까지 모두 존재해야 함
- 서로 다른 날짜의 일정이 같은 dayNumber에 포함되면 안 됨

[STOP_TYPE 규칙]
- START : 전체 여행 최초 1회만 존재 (dayNumber=1, order=1)
  - startTime 필수
- END   : 전체 여행 최종 1회만 존재 (마지막 날의 마지막 order)
  - endTime 필수
- COURSE / WAYPOINT / STAY 자유 구성
- dayNumber 1에는 STAY 생성 금지
- 여행일수 >= 2 이면, dayNumber 1..(마지막날-1) 각각에 STAY 정확히 1개 포함
  (STAY는 그날의 마지막 일정이어야 함. 단, END는 마지막 날에만 존재)

[🔥 장소 명확성 규칙 (가장 중요)]
❌ 금지 예시:
- 성남시 자전거도로
- 분당 대왕판교로
- 성남 카페거리
- 분당 맛집
- 조용한 게스트하우스

⭕ 허용 예시:
- 탄천 자전거길 (모란 → 판교 구간)
- 판교 운중천 산책로
- 수원천 자전거길 (화성행궁 구간)
- 카페 노티드 판교점
- 다운타우너 한남점

[상호명 형식 강화]

- "~카페거리", "~맛집", "~자전거도로" 단독 사용 금지.
- 지명 단독 사용 금지.
- 최소 2단어 이상 상호명으로 작성.
- 실제 간판에 있을 법한 자연스러운 상호명 사용.
- 프랜차이즈 남발 금지.

[지역 시군구 표기 규칙 (필수)]
- details의 각 항목에는 반드시 region, city 를 포함하세요.
  예) "region": "경기도", "city": "성남시"
- region/city는 travelInfo.routeRegions 또는 routeCities 값 중 하나를 반드시 사용.
- 추측으로 새로운 지역 생성 금지.
- 빈값 금지.
- COURSE도 해당 구간이 주로 속한 region/city를 기입 (출발지 기준 또는 핵심 구간 기준)

[COURSE 작성 규칙]
- COURSE는 반드시 “A → B 구간”으로 작성
- 실제 라이더가 주행할 수 있는 자전거길/하천길/순환 코스
- distanceKm는 해당 구간 기준으로 현실적으로 설정
- startTime과 endTime 필수

[WAYPOINT 규칙]
- 식사 / 카페 / 휴식 / 관광 목적이 명확해야 함
- 반드시 실존 상호명/명소명 (유저가 바로 검색해서 갈 수 있어야 함)
- startTime + endTime 필수
- ✅ 유저가 지정한 경유지(travelInfo.routeWaypoints)의 모든 항목은
  일정 어딘가에 "최소 1회 이상" 반드시 포함해야 함 (누락 금지)

[STAY 규칙 (중요)]
- 여행일수 >= 2 이면, 마지막 날을 제외한 모든 날에 STAY 1개 필수
- STAY는 해당 날짜의 "마지막 일정"이어야 함
- STAY도 반드시 실존 숙소명(호텔/게스트하우스/모텔 등)으로 작성
  예) "코트야드 바이 메리어트 서울 판교", "신라스테이 역삼"
- STAY는 startTime+endTime 중 최소 하나가 아니라, 가능하면 둘 다 기입 (체크인/휴식 시간 표현)

[추가 조건]
- memo는 “왜 이 구간/장소가 좋은지”가 드러나야 함
- Activity 선택: %s
- Stay 선택: %s

[출력 JSON 형식 예시]
{
  "summary": "여행 전체를 한 문장으로 요약",
  "travelInfo": {
    "routeRegions": %s,
    "routeCities": %s,
    "routeWaypoints": %s,
    "startPoint": "%s",
    "endPoint": "%s",
    "startDatetime": "%s",
    "endDatetime": "%s"
  },
  "schedule": {
    "hashtags": [],
    "aiKeywords": []
  },
  "days": [
    {
      "dayNumber": 1,
      "details": [
        {
          "order": 1,
          "stopType": "START",
          "placeName": "출발지 명칭",
          "region": "서울특별시",
          "city": "중구",
          "startTime": "09:00",
          "memo": "출발 준비"
        },
        {
          "order": 2,
          "stopType": "WAYPOINT",
          "placeName": "카페 노티드 판교점",
          "region": "경기도",
          "city": "성남시",
          "startTime": "10:00",
          "endTime": "11:00",
          "memo": "인기 디저트 카페에서 휴식"
        }
      ]
    }
  ]
}
""".formatted(
                // travelInfo 강제 복사 값
                routeRegionsJson,
                routeCitiesJson,
                routeWaypointsJson,
                escapeJsonString(startPoint),
                escapeJsonString(endPoint),
                escapeJsonString(startDatetime),
                escapeJsonString(endDatetime),

                // 입력(사용자 여행 정보)
                selectedSummary,

                // 시간
                startDate, startTime,
                endDate, endTime,
                endTime,
                travelDays,
                endTime,

                // day 규칙
                travelDays,
                travelDays,

                // 추가 조건
                activityText,
                wantStayText,

                // 예시 JSON에도 같은 값 삽입
                routeRegionsJson,
                routeCitiesJson,
                routeWaypointsJson,
                escapeJsonString(startPoint),
                escapeJsonString(endPoint),
                escapeJsonString(startDatetime),
                escapeJsonString(endDatetime)
        ).trim();
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    /** null/빈값이면 [] 로 정규화. 이미 [".."] 형태면 그대로 */
    private static String normalizeJsonArrayString(String jsonArrayText) {
        String t = safe(jsonArrayText);
        if (t.isBlank()) return "[]";
        String trimmed = t.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) return trimmed;
        return "[]";
    }

    private static int calcDaysInclusive(String startDate, String endDate) {
        try {
            if (startDate.isBlank() || endDate.isBlank()) return 1;
            LocalDate s = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate e = LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE);
            long diff = ChronoUnit.DAYS.between(s, e) + 1;
            return (int) Math.max(diff, 1);
        } catch (Exception ignore) {
            return 1;
        }
    }

    private static String buildSelectedSummary(
            String routeRegionsJson,
            String routeCitiesJson,
            String startDate,
            String startTime,
            String endDate,
            String endTime,
            int travelDays,
            String startPoint,
            String endPoint,
            String routeWaypointsJson,
            String activityText,
            String wantStayText
    ) {
        StringBuilder sb = new StringBuilder();

        sb.append("지역 경로(JSON): ").append(routeRegionsJson).append("\n");
        sb.append("도시 경로(JSON): ").append(routeCitiesJson).append("\n");

        sb.append("여행 기간: ")
          .append(startDate).append(startTime.isBlank() ? "" : " " + startTime)
          .append(" ~ ")
          .append(endDate).append(endTime.isBlank() ? "" : " " + endTime)
          .append(" (").append(travelDays).append("일)")
          .append("\n");

        sb.append("출발/도착 지점: ")
          .append(startPoint.isBlank() ? "-" : startPoint)
          .append(" → ")
          .append(endPoint.isBlank() ? "-" : endPoint)
          .append("\n");

        sb.append("사용자 희망 경유지(JSON): ").append(routeWaypointsJson).append("\n");

        sb.append("Activity: ").append(activityText).append("\n");
        sb.append("Stay: ").append(wantStayText);

        return sb.toString().trim();
    }

    /** JSON 문자열 값에 들어갈 따옴표/역슬래시 최소 이스케이프 */
    private static String escapeJsonString(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
