package dev.jpa.bike;

import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BikeDataLoader implements CommandLineRunner {

    private final BikeService bikeService;

    @Override
    public void run(String... args) throws Exception {

//        // -----------------------
//        // 대한민국 5권역 좌표 범위 (위도, 경도)
//        // -----------------------
//        double[][] regions = {
//            {35.0, 37.8, 126.7, 129.0},  // 수도권 + 인천/서울/경기
//            {36.5, 38.5, 127.5, 129.5},  // 강원권
//            {35.0, 37.5, 126.5, 128.5},  // 충청권
//            {35.0, 36.5, 128.0, 129.5},  // 경상권
//            {34.0, 36.0, 125.0, 127.5}   // 전라권 + 제주
//        };
//
//        int radius = 5000; // 5km 반경
//        double step = 0.2; // 위도/경도 격자 간격 (0.2도 약 22km)
//
//        Set<String> dedup = new HashSet<>(); // 이름+주소 중복 체크
//
//        for (int r = 0; r < regions.length; r++) {
//            double minLat = regions[r][0];
//            double maxLat = regions[r][1];
//            double minLng = regions[r][2];
//            double maxLng = regions[r][3];
//
//            for (double lat = minLat; lat <= maxLat; lat += step) {
//                for (double lng = minLng; lng <= maxLng; lng += step) {
//                    // -----------------
//                    // 대여소 가져오기
//                    // -----------------
//                    try {
//                        bikeService.fetchAndSaveRentalStations(lat, lng, radius);
//                        System.out.printf("권역 %d: (%f, %f) 대여소 저장 완료%n", r+1, lat, lng);
//                    } catch (Exception e) {
//                        System.out.println("대여소 데이터 가져오기 실패: " + e.getMessage());
//                    }
//
//                    // -----------------
//                    // 수리점 가져오기
//                    // -----------------
//                    try {
//                        bikeService.fetchAndSaveRepairShops(lat, lng, radius);
//                        System.out.printf("권역 %d: (%f, %f) 수리점 저장 완료%n", r+1, lat, lng);
//                    } catch (Exception e) {
//                        System.out.println("수리점 데이터 가져오기 실패: " + e.getMessage());
//                    }
//                }
//            }
//        }
//
//        System.out.println("✅ 대한민국 자전거 대여소 & 수리점 데이터 가져오기 완료!");
    }
}
