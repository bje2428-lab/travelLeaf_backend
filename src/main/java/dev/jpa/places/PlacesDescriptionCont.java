package dev.jpa.places;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
// ✅ /api prefix 환경/비환경 둘 다 받기
@RequestMapping({"/api/places/description", "/places/description"})
public class PlacesDescriptionCont {

    private final PlacesDescriptionService service;

    public PlacesDescriptionCont(PlacesDescriptionService service) {
        this.service = service;
    }

    @PostMapping("/save")
    public ResponseEntity<PlacesDescriptionDTO.Response> upsert(@RequestBody PlacesDescriptionDTO.UpsertRequest req) {
        return ResponseEntity.ok(service.upsert(req));
    }

    @GetMapping("/source/{sourceId}")
    public ResponseEntity<PlacesDescriptionDTO.Response> getBySourceId(
            @PathVariable("sourceId") String sourceId
    ) {
        return ResponseEntity.ok(service.getBySourceId(sourceId));
    }

    // (선택) 이것도 안전하게 이름 명시 추천
    @GetMapping("/id/{placeId}")
    public ResponseEntity<PlacesDescriptionDTO.Response> get(
            @PathVariable("placeId") Long placeId
    ) {
        return ResponseEntity.ok(service.get(placeId));
    }

    @DeleteMapping("/id/{placeId}")
    public ResponseEntity<String> delete(
            @PathVariable("placeId") Long placeId
    ) {
        service.delete(placeId);
        return ResponseEntity.ok("삭제 완료 placeId=" + placeId);
    }

}
