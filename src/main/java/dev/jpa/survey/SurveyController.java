package dev.jpa.survey;

import dev.jpa.survey.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/survey")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    /* 설문 목록 */
    @GetMapping
    public SurveyPageResponseDTO getSurveyList(
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "5") int size
    ) {
        return new SurveyPageResponseDTO(
            surveyService.getSurveyList(page, size)
        );
    }
    
    @GetMapping("/{surveyId}")
    public SurveyDetailDTO getSurveyDetail(
            @PathVariable("surveyId") Long surveyId
    ) {
        return surveyService.getSurveyDetail(surveyId);
    }



    /* 설문 제출 */
    @PostMapping("/submit")
    public ResponseEntity<Void> submit(
            @RequestBody SurveySubmitRequest request
    ) {
        surveyService.submitSurvey(request);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{surveyId}/result")
    public SurveyResultResponse getSurveyResult(
        @PathVariable("surveyId") Long surveyId
    ) {
        return surveyService.getSurveyResult(surveyId);
    }

}
