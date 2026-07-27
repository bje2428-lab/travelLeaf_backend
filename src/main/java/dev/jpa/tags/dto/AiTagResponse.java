package dev.jpa.tags.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiTagResponse {

    private List<String> tags;
}
