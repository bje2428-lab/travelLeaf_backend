package dev.jpa.tags.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiTagRequest {

    private String title;
    private String content;

    // 🔥 사용자가 입력한 기존 태그
    private List<String> tags;
}
