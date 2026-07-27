package dev.jpa.places;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
// ✅ 프론트가 baseURL "/api"를 붙이므로, 백엔드도 "/api/places"로 맞추는 게 가장 깔끔
@RequestMapping("/api/places")
// ✅ (선택) 프록시/배포 환경에서 CORS 막히면 켜기
// 필요 없으면 주석 처리해도 됨
//@CrossOrigin(origins = "*")
public class PlacesCont {

    private final PlacesService placesService;

    public PlacesCont(PlacesService placesService) {
        this.placesService = placesService;
    }

    /* =========================
       ✅ (기존 기능 유지) C: 장소 등록
       POST /api/places/save
    ========================= */
    @PostMapping("/save")
    public ResponseEntity<PlacesDTO.Response> createPlace(@RequestBody PlacesDTO.CreateRequest req) {
        PlacesDTO.Response res = placesService.createPlace(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    /* =========================
       ✅ (추가) Upsert (없으면 생성, 있으면 기존 반환)
       POST /api/places/upsert

       - 서비스에 upsertPlace(req)가 이미 있으면 그거 사용
       - 없으면 createPlace(req)가 내부에서 "이미 있으면 그대로 반환" 로직이 있어서
         fallback으로 createPlace(req) 호출해도 똑같이 동작 가능
    ========================= */
    @PostMapping("/upsert")
    public ResponseEntity<PlacesDTO.Response> upsertPlace(@RequestBody PlacesDTO.CreateRequest req) {
        PlacesDTO.Response res;

        try {
            // ✅ 서비스에 upsertPlace가 구현돼 있으면 이쪽이 실행됨
            res = placesService.upsertPlace(req);
        } catch (NoSuchMethodError | UnsupportedOperationException e) {
            // ✅ 서비스에 메서드 없을 때 fallback (기존 createPlace가 "있으면 반환"이면 upsert랑 동일)
            res = placesService.createPlace(req);
        }

        return ResponseEntity.ok(res);
    }

    /* =========================
       ✅ (기존 기능 유지) R: 장소 단건 조회
       GET /api/places/{placeId}
    ========================= */
    @GetMapping("/{placeId}")
    public ResponseEntity<PlacesDTO.Response> getPlace(@PathVariable("placeId") Long placeId) {
        PlacesDTO.Response res = placesService.getPlace(placeId);
        return ResponseEntity.ok(res);
    }

    /* =========================
       ✅ (추가) sourceId로 조회
       GET /api/places/source/{sourceId}
    ========================= */
    @GetMapping("/source/{sourceId}")
    public ResponseEntity<PlacesDTO.Response> getBySourceId(@PathVariable("sourceId") String sourceId) {
        PlacesDTO.Response res = placesService.getBySourceId(sourceId);
        return ResponseEntity.ok(res);
    }

    /* =========================
       ✅ (추가) 프론트 placesApi.js가 호출하는 경로 지원
       GET /api/places/source/kakao/{kakaoId}

       - kakaoId는 숫자(카카오 placeId)인데,
         너희 설계는 place_id = kakaoId(PK) 이라서 결국 getPlace랑 동일하게 처리 가능
    ========================= */
    @GetMapping("/source/kakao/{kakaoId}")
    public ResponseEntity<PlacesDTO.Response> getByKakaoId(@PathVariable("kakaoId") Long kakaoId) {
        PlacesDTO.Response res = placesService.getPlace(kakaoId);
        return ResponseEntity.ok(res);
    }

    /* =========================
       ✅ (기존 기능 유지) R: 장소 목록 조회
       GET /api/places/list
    ========================= */
    @GetMapping("/list")
    public ResponseEntity<List<PlacesDTO.Response>> getPlaces() {
        List<PlacesDTO.Response> list = placesService.getPlaces();
        return ResponseEntity.ok(list);
    }
}
