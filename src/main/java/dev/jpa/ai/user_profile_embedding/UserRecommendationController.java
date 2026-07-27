package dev.jpa.ai.user_profile_embedding;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/recommend")
public class UserRecommendationController {

    private final UserRecommendationService service;

    @GetMapping("/{userId}")
    public List<Long> recommend(@PathVariable("userId") String userId) throws Exception {
        return service.recommendPosts(userId);
    }
}
