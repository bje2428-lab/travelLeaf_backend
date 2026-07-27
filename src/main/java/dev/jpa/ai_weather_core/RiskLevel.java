package dev.jpa.ai_weather_core;

/**
 * 5단계 위험도 + UI 메타 정보
 */
public enum RiskLevel {

    VERY_SAFE("매우 안전", "#2ECC71", "🟢"),
    SAFE("안전", "#27AE60", "🟢"),
    CAUTION("주의", "#F1C40F", "🟡"),
    WARNING("경고", "#E67E22", "🟠"),
    DANGER("위험", "#E74C3C", "🔴");

    private final String label;
    private final String color;
    private final String icon;

    RiskLevel(String label, String color, String icon) {
        this.label = label;
        this.color = color;
        this.icon = icon;
    }

    public String getLabel() { return label; }
    public String getColor() { return color; }
    public String getIcon() { return icon; }

    /**
     * ✅ 미세먼지 예보 등급(좋음/보통/나쁨/매우나쁨)을 RiskLevel로 변환
     * - AirKorea 예보 API에서 흔히 "좋음", "보통", "나쁨", "매우나쁨" 형태로 옴
     * - 예외/공백/정보없음은 SAFE로 처리(서비스 안정성 목적)
     */
    public static RiskLevel fromAirGrade(String grade) {
        if (grade == null) return SAFE;
  
        // ✅ 공백/개행/이상문자 완전 제거
        String g = grade.replaceAll("\\s+", "").trim();
  
        return switch (g) {
            case "좋음" -> VERY_SAFE;
            case "보통" -> SAFE;
            case "나쁨" -> CAUTION;
            case "매우나쁨" -> WARNING;
            default -> SAFE;
        };
    }
}
