package dev.jpa.ai_request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai_request")
public class AiRequestCont {

    @Autowired
    private AiRequestService aiRequestService;

    /**
     * AI 요청 생성
     *
     * POST
     * http://localhost:9100/ai_request/create
     *
     * body:
     * {
     *   "userNo": 1,
     *   "aiType": "PLAN",
     *   "inputSummary": "지역: 서울, 기간: 3일 ..."
     * }
     */
    @PostMapping("/create")
    public AiRequest create(@RequestBody AiRequestDTO dto) {
        return aiRequestService.save(dto);
    }
}
