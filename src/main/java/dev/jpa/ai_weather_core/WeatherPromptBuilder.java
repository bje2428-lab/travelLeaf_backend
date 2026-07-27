package dev.jpa.ai_weather_core;

import dev.jpa.location.Region;

import java.time.LocalDate;

public class WeatherPromptBuilder {

    private WeatherPromptBuilder() {}

    public static String build(
            LocalDate date,
            Region region,
            WeatherResult weather,
            AirQualityResult air,
            RiskLevel riskLevel
    ) {

        StringBuilder sb = new StringBuilder();

        sb.append("""
        너는 자전거 여행 일정을 구성하는 과정에서
        특정 날짜의 "일정 지점(경유지/체류지/출발·도착지)" 기준으로
        기상 및 대기질 위험을 분석하는 전문가다.

        이 분석은 하루 전체 요약이 아니라,
        해당 날짜 일정 중 "특정 지점에서의 라이딩 또는 체류 상황"을 가정한다.

        과장되거나 추상적인 표현은 피하고,
        실제 라이딩 중 겪을 수 있는 상황을 기준으로
        현실적인 조언만 작성하라.
        """);

        /* =========================
           일정 / 분석 맥락
        ========================= */
        sb.append("\n[분석 맥락]\n");
        sb.append("- 여행 유형: 자전거 여행\n");
        sb.append("- 분석 기준: 일정 내 개별 지점 기준 분석\n");
        sb.append("- 날짜: ").append(date).append("\n");
        sb.append("- 기준 지역: ")
          .append(region != null ? region.getRegionName() : "정보 없음")
          .append("\n\n");

        /* =========================
           기상 정보
        ========================= */
        sb.append("[기상 정보]\n");
        if (weather != null) {

            if (weather.getMinTemp() != null && weather.getMaxTemp() != null) {
                sb.append("- 기온: ")
                  .append(weather.getMinTemp())
                  .append("~")
                  .append(weather.getMaxTemp())
                  .append("°C\n");
            }

            if (weather.getWindSpeed() != null) {
                sb.append("- 풍속: ")
                  .append(weather.getWindSpeed())
                  .append(" m/s\n");
            }

            if (weather.getRainProb() != null) {
                sb.append("- 강수확률: ")
                  .append(weather.getRainProb())
                  .append("%\n");
            }

            if (weather.getPrecipType() != null) {
                sb.append("- 강수형태: ")
                  .append(resolvePrecipType(weather.getPrecipType()))
                  .append("\n");
            }

            if (weather.getPrecipitation() != null) {
                sb.append("- 예상 강수량: ")
                  .append(weather.getPrecipitation())
                  .append(" mm\n");
            }

        } else {
            sb.append("- 기상 정보 없음\n");
        }
        sb.append("\n");

        /* =========================
           대기질 (등급 중심, 수치 제거)
        ========================= */
        sb.append("[대기질]\n");
        if (air != null && air.getGrade() != null && !air.getGrade().isBlank()) {
            sb.append("- 대기질 예보 등급: ")
              .append(air.getGrade())
              .append("\n");
        } else {
            sb.append("- 대기질 예보 정보 없음\n");
        }
        sb.append("\n");

        /* =========================
           위험도 판단
        ========================= */
        sb.append("[위험도 판단]\n");
        sb.append("- 종합 위험 단계: ")
          .append(riskLevel.name())
          .append(" (")
          .append(riskLevel.getLabel())
          .append(")\n\n");

        /* =========================
           작성 지침
        ========================= */
        sb.append("""
        아래 항목을 "이 지점에서 라이딩하거나 잠시 체류하는 상황"을 가정하여 작성하세요.

        1. 해당 지점에서 특히 주의해야 할 기상 또는 대기질 요소
        2. 이 조건에서 권장되는 라이딩 방식 또는 시간대
        3. 필요하다면 이 지점을 기준으로 한 우회 또는 일정 조정 제안
        4. 실제 라이더에게 바로 도움이 되는 실천 가능한 팁

        작성 규칙:
        - 모든 내용은 해당 날짜·해당 지점 기준으로 작성
        - 일반적인 하루 요약 문장은 피할 것
        - 조건이 양호하더라도 방심을 경고하는 문장을 포함
        - 각 항목은 1~2문장으로 간결하게 작성
        """);

        return sb.toString();
    }

    private static String resolvePrecipType(String pty) {
        return switch (pty) {
            case "0" -> "강수 없음";
            case "1" -> "비";
            case "2" -> "비 또는 눈";
            case "3" -> "눈";
            case "4" -> "소나기";
            default -> "알 수 없음";
        };
    }
}
