package dev.jpa.bike;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BikeService {

    private final BikeRepairShopRepository repairShopRepository;
    private final BikeRentalStationRepository rentalStationRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    private final String KAKAO_KEY = "1dbb3887ce41f8af281d237765a2f55f"; // 본인 키

    // -------------------------------
    // 기존 반경 검색 기능
    // -------------------------------

    public List<MapMarkerDto> findNearbyMarkers(double lat, double lng, double radiusKm) {
        List<MapMarkerDto> result = new ArrayList<>();

        // 수리점
        repairShopRepository.findNearby(lat, lng, radiusKm)
                .forEach(shop -> result.add(
                        MapMarkerDto.builder()
                                .id(shop.getRepairId())
                                .type("REPAIR")
                                .name(shop.getName())
                                .lat(shop.getLat())
                                .lng(shop.getLng())
                                .onsiteService(shop.getIsOnsiteService())
                                .build()
                ));

        // 대여소
        rentalStationRepository.findNearby(lat, lng, radiusKm)
                .forEach(station -> result.add(
                        MapMarkerDto.builder()
                                .id(station.getRentalId())
                                .type("RENTAL")
                                .name(station.getName())
                                .lat(station.getLat())
                                .lng(station.getLng())
                                .bikeCount(station.getBikeCount())
                                .build()
                ));

        return result;
    }

    public List<MapMarkerDto> findNearbyByPath(PathSearchRequest request) {

        Set<String> dedup = new HashSet<>();
        List<MapMarkerDto> result = new ArrayList<>();

        for (PathSearchRequest.PathPoint point : request.getPath()) {

            double lat = point.getLat();
            double lng = point.getLng();
            double radius = request.getRadius();

            // 수리점
            repairShopRepository.findNearby(lat, lng, radius)
                    .forEach(shop -> {
                        String key = "REPAIR_" + shop.getRepairId();
                        if (dedup.add(key)) {
                            result.add(
                                    MapMarkerDto.builder()
                                            .id(shop.getRepairId())
                                            .type("REPAIR")
                                            .name(shop.getName())
                                            .lat(shop.getLat())
                                            .lng(shop.getLng())
                                            .onsiteService(shop.getIsOnsiteService())
                                            .build()
                            );
                        }
                    });

            // 대여소
            rentalStationRepository.findNearby(lat, lng, radius)
                    .forEach(station -> {
                        String key = "RENTAL_" + station.getRentalId();
                        if (dedup.add(key)) {
                            result.add(
                                    MapMarkerDto.builder()
                                            .id(station.getRentalId())
                                            .type("RENTAL")
                                            .name(station.getName())
                                            .lat(station.getLat())
                                            .lng(station.getLng())
                                            .bikeCount(station.getBikeCount())
                                            .build()
                            );
                        }
                    });
        }

        return result;
    }

    // -------------------------------
    // 카카오 API로 데이터 가져오기
    // -------------------------------

    public void fetchAndSaveRepairShops(double lat, double lng, int radius) throws Exception {
        String url = "https://dapi.kakao.com/v2/local/search/keyword.json" +
                     "?query=자전거 수리점&x=" + lng + "&y=" + lat + "&radius=" + radius;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + KAKAO_KEY);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        var response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        String json = response.getBody();

        KakaoPlaceResponse kakaoResponse = objectMapper.readValue(json, KakaoPlaceResponse.class);

        // DB 중복 체크용
        Set<String> existing = new HashSet<>();
        repairShopRepository.findAll().forEach(s -> existing.add(s.getName() + "|" + s.getAddress()));

        for (KakaoPlaceResponse.Document doc : kakaoResponse.getDocuments()) {
            String key = doc.getPlaceName() + "|" + doc.getAddressName();
            if (existing.contains(key)) continue; // 중복이면 skip

            BikeRepairShop shop = BikeRepairShop.builder()
                    .name(doc.getPlaceName())
                    .address(doc.getAddressName())
                    .lat(Double.parseDouble(doc.getY()))
                    .lng(Double.parseDouble(doc.getX()))
                    .phone(doc.getPhone())
                    .openTime("09:00~18:00") // 임시값
                    .isOnsiteService(false)   // 임시값
                    .source(BikeDataSource.KAKAO_MAP)
                    .build();

            repairShopRepository.save(shop);
            existing.add(key);
        }
    }

    public void fetchAndSaveRentalStations(double lat, double lng, int radius) throws Exception {
        String url = "https://dapi.kakao.com/v2/local/search/keyword.json" +
                     "?query=자전거 대여소&x=" + lng + "&y=" + lat + "&radius=" + radius;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + KAKAO_KEY);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        var response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        String json = response.getBody();

        KakaoPlaceResponse kakaoResponse = objectMapper.readValue(json, KakaoPlaceResponse.class);

        Set<String> existing = new HashSet<>();
        rentalStationRepository.findAll().forEach(s -> existing.add(s.getName() + "|" + s.getAddress()));

        for (KakaoPlaceResponse.Document doc : kakaoResponse.getDocuments()) {

            String key = doc.getPlaceName() + "|" + doc.getAddressName();
            if (existing.contains(key)) continue; // 중복이면 skip

            int bikeCount = 40 + (int)(Math.random() * 21);
            String phone = String.format("010-%04d-%04d", (int)(Math.random() * 10000), (int)(Math.random() * 10000));

            BikeRentalStation station = BikeRentalStation.builder()
                    .name(doc.getPlaceName())
                    .address(doc.getAddressName())
                    .lat(Double.parseDouble(doc.getY()))
                    .lng(Double.parseDouble(doc.getX()))
                    .openTime("24시간")
                    .bikeCount(bikeCount)
                    .phone(phone)
                    .source(BikeDataSource.KAKAO_MAP)
                    .build();

            rentalStationRepository.save(station);
            existing.add(key);
        }
    }

    // -------------------------------
    // 범위 내 검색
    // -------------------------------

    public List<MapMarkerDto> findRentalsWithinBounds(double swLat, double swLng, double neLat, double neLng) {
        return rentalStationRepository.findWithinBounds(swLat, neLat, swLng, neLng).stream()
            .map(station -> MapMarkerDto.builder()
                .id(station.getRentalId())
                .type("RENTAL")
                .name(station.getName())
                .lat(station.getLat())
                .lng(station.getLng())
                .bikeCount(station.getBikeCount())
                .phone(station.getPhone())
                .address(station.getAddress())
                .openTime(station.getOpenTime())
                .build()
            ).toList();
    }

    public List<MapMarkerDto> findRepairsWithinBounds(double swLat, double swLng, double neLat, double neLng) {
        return repairShopRepository.findWithinBounds(swLat, neLat, swLng, neLng).stream()
            .map(shop -> MapMarkerDto.builder()
                .id(shop.getRepairId())
                .type("REPAIR")
                .name(shop.getName())
                .lat(shop.getLat())
                .lng(shop.getLng())
                .phone(shop.getPhone())
                .address(shop.getAddress())
                .openTime(shop.getOpenTime())
                .onsiteService(shop.getIsOnsiteService())
                .build()
            ).toList();
    }

    // -------------------------------
    // CRUD
    // -------------------------------

    // 수리점
    public List<BikeRepairShop> getAllRepairShops() { return repairShopRepository.findAll(); }

    public BikeRepairShop addRepairShop(BikeRepairShop shop) {
        boolean exists = repairShopRepository.existsByNameAndAddress(shop.getName(), shop.getAddress());
        if (exists) throw new RuntimeException("이미 존재하는 수리점입니다.");
        return repairShopRepository.save(shop);
    }

    public BikeRepairShop updateRepairShop(Long repairId, BikeRepairShop updatedShop) {
        return repairShopRepository.findById(repairId)
                .map(shop -> {
                    shop = shop.toBuilder()
                            .name(updatedShop.getName())
                            .address(updatedShop.getAddress())
                            .lat(updatedShop.getLat())
                            .lng(updatedShop.getLng())
                            .phone(updatedShop.getPhone())
                            .openTime(updatedShop.getOpenTime())
                            .isOnsiteService(updatedShop.getIsOnsiteService())
                            .source(updatedShop.getSource())
                            .build();
                    return repairShopRepository.save(shop);
                }).orElseThrow(() -> new RuntimeException("수리점이 존재하지 않습니다. ID: " + repairId));
    }

    public void deleteRepairShop(Long repairId) { repairShopRepository.deleteById(repairId); }

    // 대여소
    public List<BikeRentalStation> getAllRentalStations() { return rentalStationRepository.findAll(); }

    public BikeRentalStation addRentalStation(BikeRentalStation station) {
        boolean exists = rentalStationRepository.existsByNameAndAddress(station.getName(), station.getAddress());
        if (exists) throw new RuntimeException("이미 존재하는 대여소입니다.");
        return rentalStationRepository.save(station);
    }

    public BikeRentalStation updateRentalStation(Long rentalId, BikeRentalStation updatedStation) {
        return rentalStationRepository.findById(rentalId)
                .map(station -> {
                    station = station.toBuilder()
                            .name(updatedStation.getName() != null ? updatedStation.getName() : station.getName())
                            .address(updatedStation.getAddress() != null ? updatedStation.getAddress() : station.getAddress())
                            .lat(updatedStation.getLat() != null ? updatedStation.getLat() : station.getLat())
                            .lng(updatedStation.getLng() != null ? updatedStation.getLng() : station.getLng())
                            .phone(updatedStation.getPhone() != null ? updatedStation.getPhone() : station.getPhone())
                            .openTime(updatedStation.getOpenTime() != null ? updatedStation.getOpenTime() : station.getOpenTime())
                            .bikeCount(updatedStation.getBikeCount() != null ? updatedStation.getBikeCount() : station.getBikeCount())
                            .source(updatedStation.getSource() != null ? updatedStation.getSource() : station.getSource())
                            .build();
                    return rentalStationRepository.save(station);
                }).orElseThrow(() -> new RuntimeException("대여소가 존재하지 않습니다. ID: " + rentalId));
    }

    public void deleteRentalStation(Long rentalId) { rentalStationRepository.deleteById(rentalId); }

    // -------------------------------
    // 검색
    // -------------------------------

    public Page<BikeRepairShop> searchRepairShops(String keyword, Pageable pageable) {
        return repairShopRepository.findByNameContainingOrAddressContainingOrPhoneContaining(
                keyword, keyword, keyword, pageable
        );
    }

    public Page<BikeRentalStation> searchRentalStations(String keyword, Pageable pageable) {
        return rentalStationRepository.findByNameContainingOrAddressContainingOrPhoneContaining(
                keyword, keyword, keyword, pageable
        );
    }

}
