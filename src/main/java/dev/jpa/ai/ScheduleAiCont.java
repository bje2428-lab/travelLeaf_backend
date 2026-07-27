package dev.jpa.ai;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/ai/schedule", "/api/ai/schedules"})
public class ScheduleAiCont {

    private final ScheduleAiService aiService;

    public ScheduleAiCont(ScheduleAiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/{scheduleId}/summary")
    public ObjectNode summary(@PathVariable("scheduleId") Long scheduleId,
                              @RequestParam(name = "force", defaultValue = "0") int force) {
        return aiService.summary(scheduleId, force);
    }

    @PostMapping("/{scheduleId}/hashtags")
    public ObjectNode hashtags(@PathVariable("scheduleId") Long scheduleId,
                               @RequestParam(name = "force", defaultValue = "0") int force) {
        return aiService.hashtags(scheduleId, force);
    }

    @PostMapping("/{scheduleId}/day-highlights")
    public ObjectNode dayHighlights(@PathVariable("scheduleId") Long scheduleId,
                                    @RequestParam(name = "force", defaultValue = "0") int force) {
        return aiService.dayHighlights(scheduleId, force);
    }

    // ✅ NEW: 준비물/주의사항(Prep)
    @PostMapping("/{scheduleId}/prep")
    public ObjectNode prep(@PathVariable("scheduleId") Long scheduleId,
                           @RequestParam(name = "force", defaultValue = "0") int force) {
        return aiService.prep(scheduleId, force);
    }

    @PostMapping("/{scheduleId}/chat")
    public ObjectNode chat(@PathVariable("scheduleId") Long scheduleId,
                           @RequestBody ObjectNode body) {
        return aiService.chat(scheduleId, body);
    }

    // /api/ai/schedules/preview/hashtags
    @PostMapping("/preview/hashtags")
    public ObjectNode hashtagsPreview(@RequestBody ObjectNode itinerary,
                                      @RequestParam(defaultValue = "0") int force) {
        return aiService.hashtagsFromItinerary(itinerary, force);
    }

}
