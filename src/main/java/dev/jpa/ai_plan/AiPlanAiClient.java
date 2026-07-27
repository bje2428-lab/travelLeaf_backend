package dev.jpa.ai_plan;

/**
 * AI 호출부를 백엔드로 옮기기 위한 인터페이스
 *
 * ❗중요:
 * - 여기에는 URL/키/모델 등을 "임의로" 넣지 않는다.
 * - 프런트에서 사용하던 AI 호출 로직(OpenAI 호출 또는 FastAPI 호출)을
 *   이 인터페이스 구현체에 그대로 옮겨 붙이면 된다.
 */
public interface AiPlanAiClient {

    /**
     * AI에게 prompt를 보내고 결과(JSON 문자열)를 반환한다.
     *
     * @param prompt AI 프롬프트
     * @return AI 결과(JSON 문자열)
     */
    String runPlan(String prompt);
}
