package dev.jpa.survey;

import dev.jpa.reward.RewardGrantRequest;
import dev.jpa.reward.RewardService;
import dev.jpa.survey.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class SurveyService {

    private final SurveyRepository surveyRepo;
    private final SurveyQuestionRepository questionRepo;
    private final SurveyOptionRepository optionRepo;
    private final SurveyResponseRepository responseRepo;
    private final SurveyAnswerRepository answerRepo;
    private final RewardService rewardService;

    /* =====================
       설문 목록
    ===================== */
    @Transactional(readOnly = true)
    public Page<SurveyListDTO> getSurveyList(int page, int size) {
        Pageable pageable =
                PageRequest.of(page, size, Sort.by("createdAt").descending());
        return surveyRepo.findActive("Y", pageable)
                .map(SurveyListDTO::new);
    }

    /* =====================
       설문 상세
    ===================== */
    @Transactional(readOnly = true)
    public SurveyDetailDTO getSurveyDetail(Long surveyId) {

        Survey survey = surveyRepo.findById(surveyId)
                .orElseThrow(() -> new IllegalArgumentException("설문 없음"));

        SurveyDetailDTO dto = new SurveyDetailDTO();
        dto.setSurveyId(survey.getSurveyId());
        dto.setTitle(survey.getTitle());
        dto.setDescription(survey.getDescription());
        dto.setSurveyType(survey.getSurveyType());
        dto.setRewardExp(survey.getRewardPoint());
        dto.setEstTimeMin(survey.getEstTimeMin());

        List<SurveyQuestionDTO> questions =
                questionRepo.findBySurveyIdOrderByQuestionNo(surveyId)
                        .stream()
                        .map(q -> {
                            SurveyQuestionDTO qDto = new SurveyQuestionDTO();
                            qDto.setQuestionId(q.getQuestionId());
                            qDto.setQuestionNo(q.getQuestionNo());
                            qDto.setQuestionText(q.getQuestionText());
                            qDto.setQuestionType(q.getQuestionType());
                            qDto.setRequiredYn(q.getRequiredYn());

                            if (!"TEXT".equals(q.getQuestionType())) {
                                qDto.setOptions(
                                        optionRepo
                                                .findByQuestionIdOrderByOptionNo(q.getQuestionId())
                                                .stream()
                                                .map(o -> {
                                                    SurveyOptionDTO oDto =
                                                            new SurveyOptionDTO();
                                                    oDto.setOptionId(o.getOptionId());
                                                    oDto.setOptionNo(o.getOptionNo());
                                                    oDto.setOptionText(o.getOptionText());
                                                    return oDto;
                                                })
                                                .toList()
                                );
                            }
                            return qDto;
                        })
                        .toList();

        dto.setQuestions(questions);
        return dto;
    }

    /* =====================
    설문 제출 (중복 허용) + 보상
 ===================== */
 public void submitSurvey(SurveySubmitRequest req) {

     Survey survey = surveyRepo.findById(req.getSurveyId())
             .orElseThrow(() -> new IllegalArgumentException("설문 없음"));

     // ✅ 중복 제출 허용
     SurveyResponse response = new SurveyResponse();
     response.setSurveyId(survey.getSurveyId());
     response.setUserNo(req.getUserNo());
     response.setCompletedYn("Y");
     response.setCompletedAt(new Date());
     responseRepo.save(response);

     for (SurveyAnswerDTO dto : req.getAnswers()) {
         SurveyAnswer ans = new SurveyAnswer();
         ans.setResponseId(response.getResponseId());
         ans.setQuestionId(dto.getQuestionId());
         ans.setOptionId(dto.getOptionId());
         ans.setAnswerText(dto.getAnswerText());
         ans.setScoreValue(dto.getScoreValue());
         answerRepo.save(ans);
     }

     // ✅ 보상 중복 방지 (responseId 포함)
     RewardGrantRequest rewardReq = new RewardGrantRequest();
     rewardReq.setUserNo(req.getUserNo());
     rewardReq.setRewardType("EXP");
     rewardReq.setSourceType("SURVEY");
     rewardReq.setSourceKey("SURVEY_" + survey.getSurveyId() + "_" + response.getResponseId());

     // ✅ [핵심] rewardId / surveyReward / email 세팅
     if (req.getRewardId() == null) {
         throw new IllegalArgumentException("rewardId가 필요합니다.");
     }
     rewardReq.setRewardId(req.getRewardId()); // ✅ 이거 없어서 지금 터진 거임

     // surveyReward는 프론트에서 보내면 그걸 쓰고, 없으면 DB 값 사용
     Integer surveyReward = (req.getSurveyReward() != null)
             ? req.getSurveyReward()
             : survey.getRewardPoint();

     rewardReq.setSurveyReward(surveyReward);
     rewardReq.setEmail(req.getEmail());

     rewardService.grantReward(rewardReq);
 }


    /* =====================
       📊 설문 결과 집계
    ===================== */
    @Transactional(readOnly = true)
    public SurveyResultResponse getSurveyResult(Long surveyId) {

        Map<Long, SurveyQuestionResultDTO> map = new LinkedHashMap<>();

        /* ---------- 객관식 ---------- */
        List<Object[]> rows =
                surveyRepo.findSurveyChoiceResult(surveyId);

        for (Object[] r : rows) {
            Long questionId   = ((Number) r[0]).longValue();
            String questionText = (String) r[1];
            Long optionId     = ((Number) r[2]).longValue();
            String optionText = (String) r[3];
            Long count        = ((Number) r[4]).longValue();

            SurveyQuestionResultDTO q =
                    map.computeIfAbsent(questionId, id -> {
                        SurveyQuestionResultDTO dto =
                                new SurveyQuestionResultDTO();
                        dto.setQuestionId(questionId);
                        dto.setQuestionText(questionText);
                        dto.setQuestionType("SINGLE");
                        dto.setOptions(new ArrayList<>());
                        dto.setTextAnswers(new ArrayList<>());
                        return dto;
                    });

            q.getOptions().add(
                    new SurveyOptionResultDTO(optionId, optionText, count)
            );
        }

        /* ---------- TEXT ---------- */
        List<Object[]> textRows =
                surveyRepo.findSurveyTextAnswers(surveyId);

        for (Object[] r : textRows) {
            Long questionId   = ((Number) r[0]).longValue();
            String questionText = (String) r[1];
            String answerText = (String) r[2];

            SurveyQuestionResultDTO q =
                    map.computeIfAbsent(questionId, id -> {
                        SurveyQuestionResultDTO dto =
                                new SurveyQuestionResultDTO();
                        dto.setQuestionId(questionId);
                        dto.setQuestionText(questionText);
                        dto.setQuestionType("TEXT");
                        dto.setOptions(null);
                        dto.setTextAnswers(new ArrayList<>());
                        return dto;
                    });

            q.getTextAnswers().add(answerText);
        }

        return new SurveyResultResponse(
                surveyId,
                new ArrayList<>(map.values())
        );
    }
}
