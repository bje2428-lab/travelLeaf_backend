package dev.jpa.ai_weather_core;

/**
 * 기상청 응답을 내부 표준 형태로 변환한 DTO
 */
public class WeatherResult {

    private final Double minTemp;        // TMN
    private final Double maxTemp;        // TMX
    private final Double windSpeed;      // WSD
    private final Double precipitation;  // PCP (mm)
    private final Double rainProb;       // POP (%)
    private final String precipType;     // PTY (0/1/2/3/4 ...)
    private final boolean hasAlert;

    public WeatherResult(
            Double minTemp,
            Double maxTemp,
            Double windSpeed,
            Double precipitation,
            Double rainProb,
            String precipType,
            boolean hasAlert
    ) {
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.windSpeed = windSpeed;
        this.precipitation = precipitation;
        this.rainProb = rainProb;
        this.precipType = precipType;
        this.hasAlert = hasAlert;
    }

    public Double getMinTemp() {
        return minTemp;
    }

    public Double getMaxTemp() {
        return maxTemp;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }

    public Double getPrecipitation() {
        return precipitation;
    }

    /** 강수확률(%) */
    public Double getRainProb() {
        return rainProb;
    }

    /** 강수형태(PTY 코드: 0=없음, 1=비, 2=비/눈, 3=눈, 4=소나기 등) */
    public String getPrecipType() {
        return precipType;
    }

    public boolean hasAlert() {
        return hasAlert;
    }
}
