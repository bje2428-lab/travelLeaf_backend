package dev.jpa.survey.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;
import java.util.List;

@Getter
public class SurveyPageResponseDTO {

    private List<SurveyListDTO> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public SurveyPageResponseDTO(Page<SurveyListDTO> pageData) {
        this.content = pageData.getContent();
        this.page = pageData.getNumber();
        this.size = pageData.getSize();
        this.totalElements = pageData.getTotalElements();
        this.totalPages = pageData.getTotalPages();
    }
}
