package dev.jpa.tags;

import dev.jpa.tags.dto.AiTagRequest;
import dev.jpa.tags.dto.AiTagResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiTagController {

    private final AiTagService aiTagService;

    @PostMapping("/tags")
    public ResponseEntity<AiTagResponse> generateTags(
            @RequestBody AiTagRequest req
    ) {
        return ResponseEntity.ok(
                aiTagService.generateTags(req)
        );
    }
}
