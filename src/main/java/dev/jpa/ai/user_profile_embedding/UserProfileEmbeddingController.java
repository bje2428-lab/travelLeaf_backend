package dev.jpa.ai.user_profile_embedding;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/user-profile")
public class UserProfileEmbeddingController {

    private final UserProfileEmbeddingService service;

    @PostMapping("/update/{userId}")
    public String update(@PathVariable("userId") String userId) {
        service.updateUserEmbedding(userId);
        return "USER PROFILE EMBEDDING UPDATED";
    }

}
