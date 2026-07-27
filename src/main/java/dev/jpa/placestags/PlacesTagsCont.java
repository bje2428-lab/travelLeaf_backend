package dev.jpa.placestags;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
// ✅ tag/tags + /api prefix 유무 모두 대응
@RequestMapping({"/api/places/tags", "/api/places/tag", "/places/tags", "/places/tag"})
public class PlacesTagsCont {

    private final PlacesTagsService service;

    public PlacesTagsCont(PlacesTagsService service) {
        this.service = service;
    }

    @PostMapping("/save")
    public ResponseEntity<PlacesTagsDTO.ListResponse> save(@RequestBody PlacesTagsDTO.SaveRequest req) {
        return ResponseEntity.ok(service.save(req));
    }

    @GetMapping("/list/{placeId}")
    public ResponseEntity<PlacesTagsDTO.ListResponse> list(
            @PathVariable("placeId") Long placeId
    ) {
        return ResponseEntity.ok(service.list(placeId));
    }

    @GetMapping("/source/{sourceId}")
    public ResponseEntity<PlacesTagsDTO.ListResponse> getTagsBySourceId(
            @PathVariable("sourceId") String sourceId
    ) {
        return ResponseEntity.ok(service.getTagsBySourceId(sourceId));
    }

}
