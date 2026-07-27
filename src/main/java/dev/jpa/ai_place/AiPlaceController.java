package dev.jpa.ai_place;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ai/place")
@CrossOrigin(origins = {
    "http://121.160.42.34:5173",
    "http://121.160.42.105:5173",
    "http://121.160.42.26:5173",
    "http://121.160.42.71:5173",
    "http://121.160.42.22:5173"
})
public class AiPlaceController {

    private final AiPlaceService service;

    public AiPlaceController(AiPlaceService service) {
        this.service = service;
    }

    @PostMapping("/analyze")
    public AiPlaceResponseDTO analyze(@RequestParam("image") MultipartFile image) {
        return service.analyze(image);
    }
}
