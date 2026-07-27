package dev.jpa.places;

import dev.jpa.location.City;
import dev.jpa.location.CityRepository;
import dev.jpa.location.Region;
import dev.jpa.location.RegionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlacesService {

    private final PlacesRepository placesRepository;
    private final RegionRepository regionRepository;
    private final CityRepository cityRepository;

    public PlacesService(PlacesRepository placesRepository,
                         RegionRepository regionRepository,
                         CityRepository cityRepository) {
        this.placesRepository = placesRepository;
        this.regionRepository = regionRepository;
        this.cityRepository = cityRepository;
    }

    // ✅ (기존 기능 유지) C: 장소 등록 (placeId 직접 세팅 필수)
    public PlacesDTO.Response createPlace(PlacesDTO.CreateRequest req) {

        if (req.getPlaceId() == null) {
            throw new IllegalArgumentException("placeId(=kakaoId)가 필요합니다.");
        }

        // 이미 있으면 그대로 반환(중복 방지)
        Places existed = placesRepository.findById(req.getPlaceId()).orElse(null);
        if (existed != null) {
            return PlacesDTO.fromEntity(existed);
        }

        Places place = new Places();
        place.setPlaceId(req.getPlaceId()); // ✅ PK 직접 세팅

        place.setName(req.getName());
        place.setCategory(req.getCategory());
        place.setAddress(req.getAddress());
        place.setLat(req.getLat());
        place.setLng(req.getLng());
        place.setRating(req.getRating());

        place.setSourceType(req.getSourceType()); // "KAKAO" 등
        place.setSourceId(req.getSourceId());     // 필요 없으면 null

        place.setDifficulty(req.getDifficulty());
        place.setDistanceKM(req.getDistanceKM());

        if (req.getRegionId() != null) {
            Region region = regionRepository.findById(req.getRegionId()).orElse(null);
            place.setRegion(region);
        }

        if (req.getCityId() != null) {
            City city = cityRepository.findById(req.getCityId()).orElse(null);
            place.setCity(city);
        }

        placesRepository.save(place);
        return PlacesDTO.fromEntity(place);
    }

    /**
     * ✅ (추가) Upsert
     * - placeId(PK)로 먼저 찾고
     * - 없고 sourceId가 있으면 sourceId로도 한 번 더 찾고
     * - 있으면 "업데이트" / 없으면 "생성"
     *
     * ✅ 개선 포인트(기능은 그대로):
     * 1) sourceId trim 적용
     * 2) 생성 시 placeId 확정 로직 분리(가독성)
     * 3) name은 생성 시 null이면 예외(PLACES.name NOT NULL 대비) - 기존 동작 크게 안 바꾸면서 안전장치
     * 4) 업데이트는 "들어온 값만 덮어쓰기" 유지
     */
    public PlacesDTO.Response upsertPlace(PlacesDTO.CreateRequest req) {
        String sid = (req.getSourceId() == null) ? null : req.getSourceId().trim();
        boolean hasSid = (sid != null && !sid.isEmpty());

        if (req.getPlaceId() == null && !hasSid) {
            throw new IllegalArgumentException("placeId 또는 sourceId 중 하나는 필요합니다.");
        }

        Places place = null;

        // 1) placeId 우선
        if (req.getPlaceId() != null) {
            place = placesRepository.findById(req.getPlaceId()).orElse(null);
        }

        // 2) 없으면 sourceId로
        if (place == null && hasSid) {
            place = placesRepository.findBySourceId(sid).orElse(null);
        }

        // 3) 없으면 생성
        if (place == null) {
            place = new Places();

            // ✅ PK 확정: placeId가 없으면 sourceId 숫자 변환 시도
            Long newPk = req.getPlaceId();
            if (newPk == null) {
                try {
                    newPk = Long.parseLong(sid);
                } catch (Exception e) {
                    throw new IllegalArgumentException("placeId가 없고 sourceId가 숫자가 아닙니다. placeId를 보내주세요.");
                }
            }
            place.setPlaceId(newPk);

            // ✅ name NOT NULL 안전장치 (DB 제약조건/엔티티 nullable=false 대응)
            if (req.getName() == null || req.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("name은 필수입니다.(PLACES.name NOT NULL)");
            }
            // 생성 시 name은 무조건 세팅(업데이트는 아래 if에서 처리됨)
            place.setName(req.getName());
        }

        // 4) 업데이트(값이 들어온 것만 덮어쓰기)
        // ⚠️ name은 이미 생성에서 세팅했으니, 업데이트일 때만 if로 갱신
        if (req.getName() != null && !req.getName().trim().isEmpty()) place.setName(req.getName());
        if (req.getCategory() != null) place.setCategory(req.getCategory());
        if (req.getAddress() != null) place.setAddress(req.getAddress());
        if (req.getLat() != null) place.setLat(req.getLat());
        if (req.getLng() != null) place.setLng(req.getLng());
        if (req.getRating() != null) place.setRating(req.getRating());
        if (req.getSourceType() != null) place.setSourceType(req.getSourceType());
        if (hasSid) place.setSourceId(sid);
        if (req.getDifficulty() != null) place.setDifficulty(req.getDifficulty());
        if (req.getDistanceKM() != null) place.setDistanceKM(req.getDistanceKM());

        if (req.getRegionId() != null) {
            Region region = regionRepository.findById(req.getRegionId()).orElse(null);
            place.setRegion(region);
        }
        if (req.getCityId() != null) {
            City city = cityRepository.findById(req.getCityId()).orElse(null);
            place.setCity(city);
        }

        Places saved = placesRepository.save(place);
        return PlacesDTO.fromEntity(saved);
    }

    // ✅ (추가) sourceId로 조회
    @Transactional(readOnly = true)
    public PlacesDTO.Response getBySourceId(String sourceId) {
        String sid = (sourceId == null) ? null : sourceId.trim();
        if (sid == null || sid.isEmpty()) {
            throw new IllegalArgumentException("sourceId가 비었습니다.");
        }

        Places place = placesRepository.findBySourceId(sid)
                .orElseThrow(() -> new IllegalArgumentException("PLACE를 찾을 수 없습니다. sourceId=" + sid));
        return PlacesDTO.fromEntity(place);
    }

    // ✅ (기존 기능 유지) R: 장소 단건 조회
    @Transactional(readOnly = true)
    public PlacesDTO.Response getPlace(Long placeId) {
        Places place = placesRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("PLACE를 찾을 수 없습니다. placeId=" + placeId));
        return PlacesDTO.fromEntity(place);
    }

    // ✅ (기존 기능 유지) R: 전체 장소 목록 조회
    @Transactional(readOnly = true)
    public List<PlacesDTO.Response> getPlaces() {
        return placesRepository.findAll()
                .stream()
                .map(PlacesDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
