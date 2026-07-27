package dev.jpa.schedule;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ScheduleDTO {

    @Getter @Setter @NoArgsConstructor @ToString
    public static class CreateRequest {
        private Long userNo;

        private Long regionId;
        private Long cityId;

        private Long startRegionId;
        private Long startCityId;

        private Long endRegionId;
        private Long endCityId;

        private String scheduleTitle;
        private LocalDate startDate;
        private LocalDate endDate;

        // SCHEDULE 기준
        private String startTime;   // HH:MI
        private String endTime;     // HH:MI

        private Integer peopleCount;
        private Long budget;

        private String hashtags;
        private String aiKeywords;
        private String thumbnailImg;
        private String memo;

        private String isPublic;    // Y / N

        private String requestDifficulty; // 초급 / 중급 / 고급
        private String withType;

        // 공유 관련
        private String shareEnabled;    // Y / N
        private String shareScope;      // PRIVATE / PUBLIC / LINK
        private LocalDateTime shareExpiredAt;
    }

    // ===============================
    // 일정 수정 요청 DTO (null이면 기존 값 유지)
    // ===============================
    @Getter @Setter @NoArgsConstructor @ToString
    public static class UpdateRequest {
        private Long regionId;
        private Long cityId;

        private Long startRegionId;
        private Long startCityId;
        private Long endRegionId;
        private Long endCityId;

        private String scheduleTitle;
        private LocalDate startDate;
        private LocalDate endDate;

        private String startTime;
        private String endTime;

        private Integer peopleCount;
        private Long budget;

        private String hashtags;
        private String memo;
        private String isPublic;

        private String requestDifficulty;
        private String withType;

        private String shareEnabled;
        private String shareScope;
        private LocalDateTime shareExpiredAt;
    }

    @Getter @Setter @NoArgsConstructor @ToString
    public static class Response {
        private Long scheduleId;

        // 내부용
        private String scheduleCode;

        // 공유용
        private String shareCode;

        private Long userNo;
        private String userId;

        private Long regionId;
        private String regionName;

        private Long cityId;
        private String cityName;

        private Long startRegionId;
        private String startRegionName;

        private Long startCityId;
        private String startCityName;

        private Long endRegionId;
        private String endRegionName;

        private Long endCityId;
        private String endCityName;

        private String scheduleTitle;
        private LocalDate startDate;
        private LocalDate endDate;

        private String startTime;
        private String endTime;

        private Integer peopleCount;
        private Long budget;

        private String hashtags;
        private String aiKeywords;
        private String thumbnailImg;
        private String memo;
        private String isPublic;

        private String requestDifficulty;
        private String withType;

        private String shareEnabled;
        private String shareScope;
        private LocalDateTime shareExpiredAt;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response fromEntity(Schedule schedule) {
            Response dto = new Response();

            dto.scheduleId = schedule.getScheduleId();
            dto.scheduleCode = schedule.getScheduleCode();
            dto.shareCode = schedule.getShareCode();

            if (schedule.getUser() != null) {
                dto.userNo = schedule.getUser().getUserno();
                dto.userId = schedule.getUser().getUserid();
            }

            if (schedule.getRegion() != null) {
                dto.regionId = schedule.getRegion().getRegionId();
                dto.regionName = schedule.getRegion().getRegionName();
            }

            if (schedule.getCity() != null) {
                dto.cityId = schedule.getCity().getCityId();
                dto.cityName = schedule.getCity().getCityName();
            }

            if (schedule.getStartRegion() != null) {
                dto.startRegionId = schedule.getStartRegion().getRegionId();
                dto.startRegionName = schedule.getStartRegion().getRegionName();
            }

            if (schedule.getStartCity() != null) {
                dto.startCityId = schedule.getStartCity().getCityId();
                dto.startCityName = schedule.getStartCity().getCityName();
            }

            if (schedule.getEndRegion() != null) {
                dto.endRegionId = schedule.getEndRegion().getRegionId();
                dto.endRegionName = schedule.getEndRegion().getRegionName();
            }

            if (schedule.getEndCity() != null) {
                dto.endCityId = schedule.getEndCity().getCityId();
                dto.endCityName = schedule.getEndCity().getCityName();
            }

            dto.scheduleTitle = schedule.getScheduleTitle();
            dto.startDate = schedule.getStartDate();
            dto.endDate = schedule.getEndDate();
            dto.startTime = schedule.getStartTime();
            dto.endTime = schedule.getEndTime();

            dto.peopleCount = schedule.getPeopleCount();
            dto.budget = schedule.getBudget();

            dto.hashtags = schedule.getHashtags();
            dto.aiKeywords = schedule.getAiKeywords();
            dto.thumbnailImg = schedule.getThumbnailImg();
            dto.memo = schedule.getMemo();
            dto.isPublic = schedule.getIsPublic();

            dto.requestDifficulty = schedule.getRequestDifficulty();
            dto.withType = schedule.getWithType();

            dto.shareEnabled = schedule.getShareEnabled();
            dto.shareScope = schedule.getShareScope();
            dto.shareExpiredAt = schedule.getShareExpiredAt();

            dto.createdAt = schedule.getCreatedAt();
            dto.updatedAt = schedule.getUpdatedAt();

            return dto;
        }
    }
}
