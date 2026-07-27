package dev.jpa.schedule;

import dev.jpa.location.City;
import dev.jpa.location.Region;
import dev.jpa.user.User;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "SCHEDULE",
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_SCHEDULE_CODE", columnNames = "SCHEDULE_CODE"),
        @UniqueConstraint(name = "UQ_SCHEDULE_SHARE_CODE", columnNames = "SHARE_CODE")
    }
)
public class Schedule {

    /* =========================
       PK
    ========================= */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "schedule_seq")
    @SequenceGenerator(
            name = "schedule_seq",
            sequenceName = "SCHEDULE_SEQ",
            allocationSize = 1
    )
    @Column(name = "SCHEDULE_ID")
    private Long scheduleId;

    /* =========================
       FK (연관관계)
       - DB 구조:
         SCHEDULE.USER_NO (컬럼명) -> USER.USER_ID (PK) 참조
       - 그래서 referencedColumnName="USER_ID" 명시 필요
    ========================= */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USER_NO", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REGION_ID")
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CITY_ID")
    private City city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "START_REGION_ID")
    private Region startRegion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "START_CITY_ID")
    private City startCity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "END_REGION_ID")
    private Region endRegion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "END_CITY_ID")
    private City endCity;

    /* =========================
       BASIC INFO
    ========================= */
    @Column(name = "SCHEDULE_CODE", nullable = false, length = 50)
    private String scheduleCode;

    @Column(name = "SCHEDULE_TITLE", length = 200)
    private String scheduleTitle;

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @Column(name = "START_TIME", length = 5)
    private String startTime;

    @Column(name = "END_TIME", length = 5)
    private String endTime;

    @Column(name = "PEOPLE_COUNT")
    private Integer peopleCount;

    @Column(name = "BUDGET")
    private Long budget;

    /* =========================
       META / AI
    ========================= */
    @Column(name = "HASHTAGS", length = 4000)
    private String hashtags;

    @Column(name = "AI_KEYWORDS", length = 500)
    private String aiKeywords;

    /* =========================
       UI / SHARE
    ========================= */
    @Column(name = "THUMBNAIL_IMG", length = 300)
    private String thumbnailImg;

    @Column(name = "MEMO", length = 1000)
    private String memo;

    @Column(name = "IS_PUBLIC", nullable = false, length = 1)
    private String isPublic; // Y / N

    @Column(name = "REQUEST_DIFFICULTY", length = 10)
    private String requestDifficulty; // 초급 / 중급 / 고급

    @Column(name = "WITH_TYPE", length = 50)
    private String withType;

    @Column(name = "SHARE_CODE", length = 32)
    private String shareCode;

    @Column(name = "SHARE_ENABLED", nullable = false, length = 1)
    private String shareEnabled; // Y / N

    @Column(name = "SHARE_SCOPE", nullable = false, length = 10)
    private String shareScope;   // PRIVATE / PUBLIC / LINK

    @Column(name = "SHARE_EXPIRED_AT")
    private LocalDateTime shareExpiredAt;

    /* =========================
       AUDIT
    ========================= */
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    protected Schedule() {}

    /* =========================
       JPA Lifecycle
    ========================= */
    @PrePersist
    public void prePersist() {
        if (this.shareEnabled == null) this.shareEnabled = "N";
        if (this.shareScope == null) this.shareScope = "PRIVATE"; // VIEW 금지
        if (this.isPublic == null) this.isPublic = "N";

        this.createdAt = (this.createdAt == null) ? LocalDateTime.now() : this.createdAt;
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /* =========================
       Getter / Setter
    ========================= */
    public Long getScheduleId() { return scheduleId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Region getRegion() { return region; }
    public void setRegion(Region region) { this.region = region; }

    public City getCity() { return city; }
    public void setCity(City city) { this.city = city; }

    public Region getStartRegion() { return startRegion; }
    public void setStartRegion(Region startRegion) { this.startRegion = startRegion; }

    public City getStartCity() { return startCity; }
    public void setStartCity(City startCity) { this.startCity = startCity; }

    public Region getEndRegion() { return endRegion; }
    public void setEndRegion(Region endRegion) { this.endRegion = endRegion; }

    public City getEndCity() { return endCity; }
    public void setEndCity(City endCity) { this.endCity = endCity; }

    public String getScheduleCode() { return scheduleCode; }
    public void setScheduleCode(String scheduleCode) { this.scheduleCode = scheduleCode; }

    public String getScheduleTitle() { return scheduleTitle; }
    public void setScheduleTitle(String scheduleTitle) { this.scheduleTitle = scheduleTitle; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public Integer getPeopleCount() { return peopleCount; }
    public void setPeopleCount(Integer peopleCount) { this.peopleCount = peopleCount; }

    public Long getBudget() { return budget; }
    public void setBudget(Long budget) { this.budget = budget; }

    public String getHashtags() { return hashtags; }
    public void setHashtags(String hashtags) { this.hashtags = hashtags; }

    public String getAiKeywords() { return aiKeywords; }
    public void setAiKeywords(String aiKeywords) { this.aiKeywords = aiKeywords; }

    public String getThumbnailImg() { return thumbnailImg; }
    public void setThumbnailImg(String thumbnailImg) { this.thumbnailImg = thumbnailImg; }

    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }

    public String getIsPublic() { return isPublic; }
    public void setIsPublic(String isPublic) { this.isPublic = isPublic; }

    public String getRequestDifficulty() { return requestDifficulty; }
    public void setRequestDifficulty(String requestDifficulty) { this.requestDifficulty = requestDifficulty; }

    public String getWithType() { return withType; }
    public void setWithType(String withType) { this.withType = withType; }

    public String getShareCode() { return shareCode; }
    public void setShareCode(String shareCode) { this.shareCode = shareCode; }

    public String getShareEnabled() { return shareEnabled; }
    public void setShareEnabled(String shareEnabled) { this.shareEnabled = shareEnabled; }

    public String getShareScope() { return shareScope; }
    public void setShareScope(String shareScope) { this.shareScope = shareScope; }

    public LocalDateTime getShareExpiredAt() { return shareExpiredAt; }
    public void setShareExpiredAt(LocalDateTime shareExpiredAt) { this.shareExpiredAt = shareExpiredAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
